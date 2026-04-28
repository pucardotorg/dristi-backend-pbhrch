#!/usr/bin/env python3
"""
Per-module migration pipeline (8 phases).

Migrates one DRISTI service from `dristi-services/<svc>` (or
`integration-services/<svc>`) into the corresponding
`dristi-monolith/domain-<module>/src/main/java/<base-package>/<subdomain>/internal/`
tree.

Phases:
  0 Branch setup            — already in monolith/<svc> branch (caller's job)
  1 Analyze                 — emit migration_manifest.json (exists, file counts, etc.)
  2 Scaffold                — create target subdomain dirs
  3 Auto-rename             — copy main/java -> target/internal with package rewrite
  4 Deduplicate             — delete local copies of protected classes; rewrite imports
  5 Detect REST calls       — flag intra-DRISTI ServiceRequestRepository / RestTemplate
                              usage for manual conversion later
  6 Test migration          — copy src/test/java -> target/test with package rewrite
  7 Validate                — gate checks: no banned packages, no protected class
                              duplicates, no service application.yml, mvn -pl validate
  8 Wire module deps        — add dristi-common dep to the target domain module pom.xml
                              if not already present

Phase 8 (commit + PR) is left to the caller — running this script does not
auto-commit so the diff can be reviewed first.

Usage:
  python3 run_module_migration.py \\
      --service lock-svc \\
      --module case-lifecycle \\
      --subdomain locksvc \\
      [--phase 1,2,3,4,5,6,7,8]
"""

from __future__ import annotations

import argparse
import json
import os
import re
import subprocess
from collections import Counter, defaultdict
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parents[3]
MONOLITH_ROOT = REPO_ROOT / "dristi-monolith"
COMMON_PKG = "org.pucar.dristi.common"

# Marker that opts a target file out of overwrite during re-runs of the
# pipeline (mirrors the dristi-common Phase 3 guard).
CURATED_MARKER = "// HAND-CURATED"

# Hosts that legitimately stay as REST calls (DIGIT/eGov platform). Any
# `*Host` config that contains one of these is NOT flagged as an
# intra-DRISTI candidate by Phase 5.
EGOV_HOST_TOKENS = (
    "egov", "digit", "individual", "user", "mdms", "filestore",
    "idgen", "workflow", "shortener", "shortner", "tracer",
    "encryption", "keycloak", "billing", "hrms", "localization",
    "url",  # urlShortener variants
)

PROTECTED_CLASSES = {
    "MdmsUtil": "util",
    "IdgenUtil": "util",
    "WorkflowUtil": "util",
    "FileStoreUtil": "util",
    "UserUtil": "util",
    "UrlShortenerUtil": "util",
    "IndividualUtil": "util",
    "ResponseInfoFactory": "util",
    "Producer": "kafka",
    "KafkaProducerService": "kafka",
    "ServiceRequestRepository": "repository",
    "AuditDetails": "models",
    "ResponseInfo": "models",
    "Document": "models",
    "Individual": "models.individual",
}

# "Banned" = legacy DRISTI service-internal package roots that should have
# been rewritten by Phase 3. NOT in this list (and intentionally so):
#   - digit.models.coremodels.*  — egov-services digit-models library
#   - digit.models.bankaccount.* — same external library
#   - digit.models.individual.*  — same external library
# These are real shared DTOs that ride alongside the DRISTI codebase.
BANNED_IMPORT_PREFIXES = (
    "digit.config.",
    "digit.repository.",
    "digit.service.",
    "digit.util.",
    "digit.web.",
    "digit.kafka.",
    "digit.enrichment.",
    "digit.validators.",
    "digit.scheduling.",
    "digit.annotation.",
    "pucar.config.",
    "pucar.repository.",
    "pucar.service.",
    "pucar.util.",
    "pucar.web.",
    "pucar.kafka.",
    "notification.",
    "drishti.payment.",
    "com.egov.icops",
    "com.dristi.njdg",
    "org.egov.eTreasury.",
    "org.egov.transformer.",
    "org.drishti.esign.",
    "com.pucar.drishti.",
)

PACKAGE_RE = re.compile(r"^\s*package\s+([\w.]+)\s*;", re.MULTILINE)
IMPORT_RE = re.compile(r"^\s*import\s+(static\s+)?([\w.]+(?:\.\*)?)\s*;", re.MULTILINE)


# --- helpers ----------------------------------------------------------------


def ensure_jdk_17() -> None:
    """Maven needs a JDK with javac (not just the JRE). Pick the first
    JDK 17.x we find on disk if javac is not on PATH."""
    if shutil_which("javac"):
        return
    candidates = sorted(Path.home().glob(".jdks/corretto-17*"))
    if not candidates:
        raise SystemExit(
            "ERROR: javac not found; install a JDK 17 or set JAVA_HOME/PATH"
        )
    jdk = candidates[-1]
    os.environ["JAVA_HOME"] = str(jdk)
    os.environ["PATH"] = f"{jdk}/bin:{os.environ.get('PATH', '')}"
    print(f"[precondition] using JDK at {jdk}")


def shutil_which(cmd: str) -> str | None:
    from shutil import which
    return which(cmd)


def find_service_dir(service: str) -> Path:
    for parent in ("dristi-services", "integration-services"):
        candidate = REPO_ROOT / parent / service
        if candidate.is_dir():
            return candidate
    raise SystemExit(f"ERROR: service '{service}' not found")


def detect_current_package(service_dir: Path) -> str:
    """Find the most common top-level package across the service's main java."""
    pkgs = Counter()
    for java in (service_dir / "src" / "main" / "java").rglob("*.java"):
        try:
            text = java.read_text(encoding="utf-8")
        except (OSError, UnicodeDecodeError):
            continue
        m = PACKAGE_RE.search(text)
        if m:
            top = m.group(1).split(".")[0]
            # If clearly nested under a 2-segment marker use that
            if top in {"org", "com"}:
                # com.foo.bar -> com.foo
                segments = m.group(1).split(".")
                pkgs[".".join(segments[:3]) if len(segments) >= 3 else m.group(1)] += 1
            else:
                pkgs[top] += 1
    if not pkgs:
        raise SystemExit("ERROR: could not detect current package root")
    return pkgs.most_common(1)[0][0]


def java_files(root: Path) -> list[Path]:
    return [p for p in root.rglob("*.java") if p.is_file()]


DOMAIN_MODULE_SEGMENTS = {
    "common", "caselifecycle", "identity", "integration", "payments",
}


def rewrite_text(text: str, current_pkg: str, target_pkg: str) -> str:
    """Rewrite three styles of references from the current package to the
    target package:
      1. `package <currentPkg>...;` declaration
      2. `import [static] <currentPkg>...;` lines
      3. inline fully-qualified type references in code (e.g.
         `private org.pucar.dristi.web.models.X foo;`)

    For (3), references that point at another monolith module
    (`org.pucar.dristi.common.*`, `org.pucar.dristi.caselifecycle.*`, etc.)
    are NOT rewritten — those resolve through normal import chains."""
    cur_re = re.escape(current_pkg)
    new = re.sub(
        rf"^(\s*package\s+){cur_re}\b",
        rf"\1{target_pkg}",
        text,
        flags=re.MULTILINE,
    )
    new = re.sub(
        rf"^(\s*import\s+(?:static\s+)?){cur_re}\b",
        rf"\1{target_pkg}",
        new,
        flags=re.MULTILINE,
    )

    # Inline FQN rewrite. Pattern: <currentPkg>.<seg>.<rest>
    # The lookbehind `(?<![\w.${])` ensures we match `pucar.` only at the
    # start of a fully-qualified Java path — NEVER as a substring inside:
    #   - an already-rewritten path: `org.pucar.dristi.caselifecycle...`
    #     (preceded by `.`, blocked)
    #   - a Spring property placeholder: `@Value("${pucar.lock.duration}")`
    #     (preceded by `{`, blocked — and `$` for safety on `$pucar...`)
    inline_pattern = re.compile(rf"(?<![\w.${{]){cur_re}\.([a-zA-Z_][\w]*)\.")

    def _replace_inline(m: re.Match) -> str:
        seg = m.group(1)
        if seg in DOMAIN_MODULE_SEGMENTS:
            return m.group(0)
        return f"{target_pkg}.{seg}."

    new = inline_pattern.sub(_replace_inline, new)
    return new


def rewrite_protected_imports(text: str, owning_pkg: str) -> str:
    """Replace remaining local protected-class imports with dristi-common."""
    new = text
    for cls, sub in PROTECTED_CLASSES.items():
        new_pkg = f"{COMMON_PKG}.{sub}"
        # plain import
        new = re.sub(
            rf"^(\s*import\s+){re.escape(owning_pkg)}\.\w+\.{re.escape(cls)}\s*;",
            rf"\1{new_pkg}.{cls};",
            new,
            flags=re.MULTILINE,
        )
        # static import
        new = re.sub(
            rf"^(\s*import\s+static\s+){re.escape(owning_pkg)}\.\w+\.{re.escape(cls)}\.",
            rf"\1{new_pkg}.{cls}.",
            new,
            flags=re.MULTILINE,
        )
    return new


# --- phases -----------------------------------------------------------------


def phase_1_analyze(args, service_dir: Path) -> dict:
    main_dir = service_dir / "src" / "main" / "java"
    test_dir = service_dir / "src" / "test" / "java"
    main_files = java_files(main_dir) if main_dir.exists() else []
    test_files = java_files(test_dir) if test_dir.exists() else []
    current_pkg = detect_current_package(service_dir)

    target_module_pkg = re.sub(r"-", "", args.module)  # case-lifecycle -> caselifecycle
    target_pkg = f"org.pucar.dristi.{target_module_pkg}.{args.subdomain}"

    has_main = any("@SpringBootApplication" in f.read_text(encoding="utf-8")
                   for f in main_files
                   if f.suffix == ".java")

    # Detect duplicates of protected classes in this service
    duplicated = []
    for cls in PROTECTED_CLASSES:
        for f in main_files:
            if f.stem == cls and re.search(rf"\bclass\s+{cls}\b", f.read_text(encoding="utf-8")):
                duplicated.append(cls)
                break

    # Detect ServiceRequestRepository usage (REST candidates)
    rest_call_files = []
    for f in main_files:
        text = f.read_text(encoding="utf-8")
        if "serviceRequestRepository" in text or "ServiceRequestRepository" in text:
            rest_call_files.append(str(f.relative_to(REPO_ROOT)))

    manifest = {
        "service": args.service,
        "source_dir": str(service_dir.relative_to(REPO_ROOT)),
        "target_module": args.module,
        "target_subdomain": args.subdomain,
        "current_package": current_pkg,
        "target_package": target_pkg,
        "main_file_count": len(main_files),
        "test_file_count": len(test_files),
        "has_spring_boot_main": has_main,
        "duplicated_protected_classes": duplicated,
        "rest_call_files": rest_call_files,
    }
    out = REPO_ROOT / "scripts" / "migration" / "per_module" / "output" / f"{args.service}_manifest.json"
    out.parent.mkdir(parents=True, exist_ok=True)
    out.write_text(json.dumps(manifest, indent=2))
    print(f"Phase 1 (analyze): wrote {out.relative_to(REPO_ROOT)}")
    print(f"  main files={len(main_files)}, test files={len(test_files)}")
    print(f"  current package: {current_pkg}")
    print(f"  target package:  {target_pkg}")
    print(f"  has @SpringBootApplication: {has_main}")
    print(f"  protected dups: {duplicated}")
    print(f"  REST candidate files: {len(rest_call_files)}")
    return manifest


def phase_2_scaffold(manifest: dict) -> Path:
    target_pkg = manifest["target_package"]
    base_dir = MONOLITH_ROOT / f"domain-{manifest['target_module']}" / "src" / "main" / "java"
    pkg_path = base_dir / target_pkg.replace(".", "/") / "internal"
    pkg_path.mkdir(parents=True, exist_ok=True)
    test_path = (
        MONOLITH_ROOT
        / f"domain-{manifest['target_module']}"
        / "src"
        / "test"
        / "java"
        / target_pkg.replace(".", "/")
        / "internal"
    )
    test_path.mkdir(parents=True, exist_ok=True)
    print(f"Phase 2 (scaffold): created {pkg_path.relative_to(REPO_ROOT)}")
    return pkg_path


def phase_3_auto_rename(manifest: dict, target_dir: Path) -> int:
    src = REPO_ROOT / manifest["source_dir"] / "src" / "main" / "java"
    cur = manifest["current_package"]
    new = f"{manifest['target_package']}.internal"
    cur_path = cur.replace(".", "/")
    src_pkg_root = src / cur_path
    if not src_pkg_root.exists():
        # The package may be split — fall back to scanning all files
        print(f"  source package root {src_pkg_root} not found, scanning all java files")
        files = java_files(src)
    else:
        files = java_files(src_pkg_root)

    copied = 0
    skipped_curated = 0
    for f in files:
        text = f.read_text(encoding="utf-8")
        if "@SpringBootApplication" in text:
            print(f"  skipping {f.relative_to(REPO_ROOT)} (has @SpringBootApplication)")
            continue
        rel = f.relative_to(src)
        if not str(rel).startswith(cur_path):
            print(f"  skipping {rel} (not under {cur_path})")
            continue
        rest = str(rel)[len(cur_path) + 1 :] if str(rel) != cur_path else f.name
        dest = target_dir / rest
        # HAND-CURATED guard: never overwrite a file the human has marked.
        if dest.exists() and CURATED_MARKER in dest.read_text(encoding="utf-8"):
            skipped_curated += 1
            continue
        dest.parent.mkdir(parents=True, exist_ok=True)
        dest.write_text(rewrite_text(text, cur, new), encoding="utf-8")
        copied += 1
    print(
        f"Phase 3 (auto-rename): copied {copied} files into {target_dir.relative_to(REPO_ROOT)}"
        + (f"; preserved {skipped_curated} hand-curated files" if skipped_curated else "")
    )
    return copied


def _add_import_if_missing(text: str, fqcn: str) -> str:
    """Insert `import <fqcn>;` after the first import block (or after the
    package declaration). No-op if the import is already present."""
    if re.search(rf"^\s*import\s+{re.escape(fqcn)}\s*;", text, re.MULTILINE):
        return text
    new_import = f"import {fqcn};"
    # Insert after the last existing import, otherwise after the package line.
    last_import = list(re.finditer(r"^\s*import\s+[^;]+;\s*$", text, re.MULTILINE))
    if last_import:
        ins = last_import[-1].end()
        return text[:ins] + "\n" + new_import + text[ins:]
    pkg_match = re.search(r"^\s*package\s+[^;]+;\s*$", text, re.MULTILINE)
    if pkg_match:
        ins = pkg_match.end()
        return text[:ins] + "\n\n" + new_import + text[ins:]
    return new_import + "\n" + text


def _ensure_unique_bean_name(path: Path, subdomain: str, class_name: str) -> bool:
    """Re-stamp `@Component` / `@Service` / `@Repository` / `@Configuration`
    on a class with an explicit, subdomain-prefixed bean name so it doesn't
    collide with another class of the same simple name elsewhere in the
    monolith.

    Example: `@Component` on cases/.../FileStoreUtil → `@Component("casesFileStoreUtil")`.

    No-op if an explicit bean name is already present, or if the class
    has no Spring stereotype annotation. Returns True on rewrite.
    """
    bean_name = subdomain + class_name[0].upper() + class_name[1:]
    text = path.read_text(encoding="utf-8")
    # Match a stereotype annotation that is NOT already given an explicit
    # value. Negative-lookahead avoids re-stamping `@Component("foo")`.
    pattern = re.compile(
        r"^(\s*@(?:Component|Service|Repository|Configuration))(?!\s*\()(\s*$)",
        re.MULTILINE,
    )
    new_text, n = pattern.subn(rf'\1("{bean_name}")', text)
    if n:
        path.write_text(new_text, encoding="utf-8")
    return n > 0


def _resolve_cross_subdomain_bean_collisions(target_module: str) -> int:
    """Spring fails fast if two `@Component`-annotated classes derive the
    same default bean name (which happens whenever two service-internal
    classes share a simple class name across subdomains, e.g. case's
    `ResponseInfoFactory` and lock-svc's `ResponseInfoFactory`).

    Walk the whole domain module and auto-prefix bean names for any
    simple class name that appears in 2+ subdomains. Idempotent."""
    domain_root = MONOLITH_ROOT / f"domain-{target_module}" / "src" / "main" / "java"
    if not domain_root.exists():
        return 0

    # subdomain is the segment immediately after `<base>/<base>` (e.g.
    # `org/pucar/dristi/caselifecycle/<subdomain>/internal/...`). Walk and
    # group simple class names by (subdomain, path).
    by_simple_name: dict[str, list[tuple[str, Path]]] = defaultdict(list)
    for java in domain_root.rglob("*.java"):
        text = java.read_text(encoding="utf-8")
        m = PACKAGE_RE.search(text)
        if not m:
            continue
        pkg = m.group(1).split(".")
        # Find segment "internal" — the subdomain is one before it.
        if "internal" not in pkg:
            continue
        idx = pkg.index("internal")
        if idx == 0:
            continue
        subdomain = pkg[idx - 1]
        by_simple_name[java.stem].append((subdomain, java))

    fixed = 0
    for simple_name, occurrences in by_simple_name.items():
        unique_subdomains = {sd for sd, _ in occurrences}
        if len(unique_subdomains) <= 1:
            continue
        for subdomain, path in occurrences:
            if _ensure_unique_bean_name(path, subdomain, simple_name):
                fixed += 1
    return fixed


def _public_methods(text: str) -> set[tuple[str, int]]:
    """Best-effort signature extraction. Returns a set of
    (method-name, parameter-count) tuples. Different overloads of the
    same name show up as distinct entries — two methods with the same
    name but different arities are treated as different surface."""
    methods = re.findall(
        r"public\s+(?:static\s+|final\s+|abstract\s+)*[\w<>,?\s\[\]]+?\s+(\w+)\s*\(([^)]*)\)",
        text,
    )
    out: set[tuple[str, int]] = set()
    for name, params in methods:
        argc = 0 if not params.strip() else params.count(",") + 1
        out.add((name, argc))
    return out


def phase_4_deduplicate(manifest: dict, target_dir: Path) -> tuple[int, int, int]:
    """Delete local copies of protected classes that already live in
    dristi-common, then rewrite remaining imports + insert missing imports
    in same-package references.

    A local protected class that has *more* public methods than the
    canonical is preserved (renamed to <Subdomain><Class>Helper) and
    flagged in `<service>_followups.txt` so a reviewer can decide whether
    to lift the extra methods into dristi-common or keep them service-local.
    """
    deleted = 0
    rewritten = 0
    auto_imported = 0
    cur_pkg = manifest["current_package"]
    new_pkg = f"{manifest['target_package']}.internal"
    followups: list[str] = []

    deleted_classes_by_pkg: dict[str, set[str]] = {}
    kept_classes: set[str] = set()  # protected classes left in target tree (extra methods)
    common_root = MONOLITH_ROOT / "dristi-common" / "src" / "main" / "java"
    for f in list(target_dir.rglob("*.java")):
        if f.stem not in PROTECTED_CLASSES:
            continue
        text = f.read_text(encoding="utf-8")
        if not re.search(rf"\bclass\s+{f.stem}\b", text):
            continue

        sub = PROTECTED_CLASSES[f.stem]
        canonical = common_root / "org" / "pucar" / "dristi" / "common"
        for seg in sub.split("."):
            canonical = canonical / seg
        canonical = canonical / f"{f.stem}.java"
        if canonical.exists():
            local_methods = _public_methods(text)
            canonical_methods = _public_methods(canonical.read_text(encoding="utf-8"))
            # Drop the constructor entry (same name as the class).
            local_methods = {m for m in local_methods if m[0] != f.stem}
            extra = local_methods - canonical_methods
            if extra:
                followups.append(
                    f"{f.relative_to(REPO_ROOT)} has {len(extra)} method(s)/"
                    f"overload(s) absent from canonical: {sorted(extra)}"
                )
                kept_classes.add(f.stem)
                # Avoid a bean-name collision at startup: by default
                # `@Component`/`@Service`/`@Repository` derive the bean name
                # from the simple class name, which collides with the
                # canonical's bean. Re-stamp the local class with a
                # subdomain-prefixed bean name (e.g. `casesFileStoreUtil`).
                _ensure_unique_bean_name(f, manifest["target_subdomain"], f.stem)
                continue  # keep this file — reviewer decides

        m = PACKAGE_RE.search(text)
        pkg = m.group(1) if m else ""
        deleted_classes_by_pkg.setdefault(pkg, set()).add(f.stem)
        f.unlink()
        deleted += 1

    if followups:
        out = REPO_ROOT / "scripts" / "migration" / "per_module" / "output" / f"{manifest['service']}_followups.txt"
        out.write_text("\n".join(followups) + "\n", encoding="utf-8")
        for fu in followups:
            print(f"  KEEP-WITH-FOLLOWUP: {fu}")

    # Apply rewrites to BOTH the target main tree and the migrated test tree.
    # Tests live next to the production code's package — same protected-class
    # imports need the same redirection.
    test_target_dir = (
        MONOLITH_ROOT
        / f"domain-{manifest['target_module']}"
        / "src"
        / "test"
        / "java"
        / new_pkg.replace(".", "/")
    )
    sweep_roots = [target_dir]
    if test_target_dir.exists():
        sweep_roots.append(test_target_dir)

    for root in sweep_roots:
        for f in root.rglob("*.java"):
            text = f.read_text(encoding="utf-8")
            new = text
            for cls, sub in PROTECTED_CLASSES.items():
                # Skip kept-with-followup classes — their callers may use
                # extra service-only methods that the canonical doesn't have.
                if cls in kept_classes:
                    continue
                common_fqcn = f"{COMMON_PKG}.{sub}.{cls}"
                new = re.sub(
                    rf"^(\s*import\s+){re.escape(cur_pkg)}\.\w+(?:\.\w+)*\.{re.escape(cls)}\s*;",
                    rf"\1{common_fqcn};",
                    new,
                    flags=re.MULTILINE,
                )
                new = re.sub(
                    rf"^(\s*import\s+){re.escape(new_pkg)}\.\w+(?:\.\w+)*\.{re.escape(cls)}\s*;",
                    rf"\1{common_fqcn};",
                    new,
                    flags=re.MULTILINE,
                )
                new = re.sub(
                    rf"^(\s*import\s+static\s+){re.escape(cur_pkg)}\.\w+(?:\.\w+)*\.{re.escape(cls)}\.",
                    rf"\1{common_fqcn}.",
                    new,
                    flags=re.MULTILINE,
                )
            if new != text:
                rewritten += 1

            # Auto-import insertion for any deleted protected class that the
            # file references by simple name without an explicit single-class
            # import. Catches three cases at once:
            #   - same-package reference (no import at all)
            #   - wildcard import `internal.util.*` that no longer covers the
            #     class because it was deleted from that package
            #   - file imports the OLD package's wildcard before Phase 3
            #     rewrote it to point at the now-empty target package
            #
            # Skips classes that are KEPT under follow-up review — those
            # callers may use the local class's extra methods, so steering
            # them at the canonical would silently change behaviour.
            all_deleted_classes = {
                c for s in deleted_classes_by_pkg.values() for c in s
            } - kept_classes
            for cls in all_deleted_classes:
                if re.search(rf"\b{cls}\b", new) and not re.search(
                    rf"^\s*import\s+(?:static\s+)?\S+\.{cls}\s*;", new, re.MULTILINE
                ):
                    sub = PROTECTED_CLASSES[cls]
                    fqcn = f"{COMMON_PKG}.{sub}.{cls}"
                    new = _add_import_if_missing(new, fqcn)
                    auto_imported += 1

            if new != text:
                f.write_text(new, encoding="utf-8")
    # Cross-subdomain bean-name collision resolution. Spring fails fast on
    # `responseInfoFactory` (case + lock-svc both have one). Auto-prefix the
    # bean name in EVERY file whose simple class name appears in 2+
    # subdomains of this domain module.
    bean_renamed = _resolve_cross_subdomain_bean_collisions(manifest["target_module"])

    print(
        f"Phase 4 (deduplicate): deleted={deleted} protected-class copies, "
        f"rewrote={rewritten} imports, auto-imported={auto_imported}"
        + (f", uniquified-bean-names={bean_renamed}" if bean_renamed else "")
    )
    return deleted, rewritten, auto_imported


def phase_5_detect_rest(manifest: dict, target_dir: Path) -> list[str]:
    """Detect intra-DRISTI REST calls — those that target other DRISTI services
    (case, hearing, order, etc.) and should become direct method calls. Hosts
    that match the eGov/DIGIT platform skip list are NOT flagged: those stay
    as REST calls forever."""
    findings: list[str] = []
    for f in target_dir.rglob("*.java"):
        text = f.read_text(encoding="utf-8")
        if "serviceRequestRepository" not in text and "RestTemplate" not in text:
            continue
        suspicious = re.findall(
            r"config[s]?\.get([A-Za-z]+Host)\(\)|configs?\.get([A-Za-z]+Path)\(\)",
            text,
        )
        if not suspicious:
            continue
        host_methods = sorted({a or b for a, b in suspicious})
        # Drop any method whose name matches an egov/digit token — those are
        # legitimate platform calls.
        intra_dristi = [
            h for h in host_methods
            if not any(token in h.lower() for token in EGOV_HOST_TOKENS)
        ]
        if intra_dristi:
            findings.append(f"{f.relative_to(REPO_ROOT)}  uses {intra_dristi}")
    out = REPO_ROOT / "scripts" / "migration" / "per_module" / "output" / f"{manifest['service']}_rest_calls.txt"
    out.write_text("\n".join(findings) + ("\n" if findings else ""), encoding="utf-8")
    print(f"Phase 5 (REST detection): {len(findings)} intra-DRISTI candidate(s); see {out.relative_to(REPO_ROOT)}")
    return findings


def phase_6_test_migration(manifest: dict) -> int:
    src = REPO_ROOT / manifest["source_dir"] / "src" / "test" / "java"
    if not src.exists():
        print("Phase 6 (test migration): no test tree, skipping")
        return 0
    cur = manifest["current_package"]
    new = f"{manifest['target_package']}.internal"
    cur_path = cur.replace(".", "/")
    target_test_dir = (
        MONOLITH_ROOT
        / f"domain-{manifest['target_module']}"
        / "src"
        / "test"
        / "java"
        / new.replace(".", "/")
    )
    target_test_dir.mkdir(parents=True, exist_ok=True)

    src_pkg_root = src / cur_path
    files = java_files(src_pkg_root) if src_pkg_root.exists() else java_files(src)
    copied = 0
    skipped_curated = 0
    for f in files:
        text = f.read_text(encoding="utf-8")
        rel = f.relative_to(src)
        if not str(rel).startswith(cur_path):
            continue
        rest = str(rel)[len(cur_path) + 1 :]
        if Path(rest).stem.replace("Test", "").replace("Tests", "") in PROTECTED_CLASSES:
            continue
        dest = target_test_dir / rest
        if dest.exists() and CURATED_MARKER in dest.read_text(encoding="utf-8"):
            skipped_curated += 1
            continue

        # Just package rename here. Protected-class import rewrites and
        # auto-imports are applied by Phase 4 once the test files are in
        # place (so its kept_classes / deleted_classes state is consistent
        # across the main and test trees).
        dest.parent.mkdir(parents=True, exist_ok=True)
        dest.write_text(rewrite_text(text, cur, new), encoding="utf-8")
        copied += 1
    print(
        f"Phase 6 (test migration): copied {copied} test files"
        + (f"; preserved {skipped_curated} hand-curated tests" if skipped_curated else "")
    )
    return copied


def phase_7_validate(manifest: dict, target_dir: Path) -> tuple[int, list[str]]:
    fails: list[str] = []

    # Gate 1: No banned import prefixes in target tree
    bad_imports: list[str] = []
    for f in target_dir.rglob("*.java"):
        for m in IMPORT_RE.finditer(f.read_text(encoding="utf-8")):
            imp = m.group(2).rstrip(".*")
            if any(imp.startswith(p) for p in BANNED_IMPORT_PREFIXES):
                bad_imports.append(f"{f.relative_to(REPO_ROOT)}: {imp}")
    if bad_imports:
        fails.append(f"Gate 1 (banned imports): {len(bad_imports)} hits")
        for b in bad_imports[:5]:
            fails.append(f"  - {b}")
    else:
        print("PASS Gate 1: no banned-package imports")

    # Gate 2: No protected-class duplicates inside target — except those
    # explicitly marked KEEP-WITH-FOLLOWUP by Phase 4 (extra service-only
    # methods not in canonical).
    followup_path = REPO_ROOT / "scripts" / "migration" / "per_module" / "output" / f"{manifest['service']}_followups.txt"
    followup_paths = set()
    if followup_path.exists():
        for line in followup_path.read_text(encoding="utf-8").splitlines():
            if not line.strip():
                continue
            followup_paths.add(line.split()[0])
    leaks = [
        f for f in target_dir.rglob("*.java")
        if f.stem in PROTECTED_CLASSES and str(f.relative_to(REPO_ROOT)) not in followup_paths
    ]
    if leaks:
        fails.append(f"Gate 2 (protected dups): {len(leaks)} unresolved files")
    else:
        kept = len(followup_paths)
        suffix = f" ({kept} kept under follow-up review)" if kept else ""
        print(f"PASS Gate 2: no unresolved protected-class duplicates{suffix}")

    # Gate 3: No application.yml in module
    yml_in_module = list(
        (MONOLITH_ROOT / f"domain-{manifest['target_module']}" / "src" / "main" / "resources").glob("application*.y*ml")
    ) if (MONOLITH_ROOT / f"domain-{manifest['target_module']}" / "src" / "main" / "resources").exists() else []
    if yml_in_module:
        fails.append(f"Gate 3 (config guard): {len(yml_in_module)} application.yml under module")
    else:
        print("PASS Gate 3: no module-level application.yml")

    # Gate 4: All target files declare correct package
    expected_prefix = f"{manifest['target_package']}.internal"
    bad_pkg = []
    for f in target_dir.rglob("*.java"):
        m = PACKAGE_RE.search(f.read_text(encoding="utf-8"))
        if not m or not m.group(1).startswith(expected_prefix):
            bad_pkg.append(f"{f.relative_to(REPO_ROOT)} -> {m.group(1) if m else '<no package>'}")
    if bad_pkg:
        fails.append(f"Gate 4 (package naming): {len(bad_pkg)} files have wrong package")
        for b in bad_pkg[:5]:
            fails.append(f"  - {b}")
    else:
        print("PASS Gate 4: all target files in expected package")

    # Gate 5: dristi-common compile still passes
    print("Running mvn compile ...")
    result = subprocess.run(
        ["mvn", "-B", "-q", "-pl", "dristi-common", "-am", "compile"],
        cwd=MONOLITH_ROOT,
        capture_output=True,
        text=True,
    )
    if result.returncode != 0:
        fails.append("Gate 5 (mvn compile): dristi-common no longer compiles")
        fails.append("  stderr tail: " + (result.stderr.splitlines() or [""])[-1])
    else:
        print("PASS Gate 5: dristi-common compiles")

    if fails:
        print()
        print("FAIL — gates:")
        for f in fails:
            print(f"  {f}")
    return len(fails), fails


DEP_RE = re.compile(
    r"<dependency>\s*"
    r"<groupId>([^<]+)</groupId>\s*"
    r"<artifactId>([^<]+)</artifactId>"
    r"(?:[\s\S]*?)</dependency>",
    re.MULTILINE,
)


def _format_dep(group: str, artifact: str, version: str | None, scope: str | None,
                optional: str | None) -> str:
    lines = [
        "        <dependency>",
        f"            <groupId>{group}</groupId>",
        f"            <artifactId>{artifact}</artifactId>",
    ]
    if version:
        lines.append(f"            <version>{version}</version>")
    if scope:
        lines.append(f"            <scope>{scope}</scope>")
    if optional:
        lines.append(f"            <optional>{optional}</optional>")
    lines.append("        </dependency>")
    return "\n".join(lines)


def _parse_pom_deps(pom_text: str) -> list[dict[str, str]]:
    """Extract all <dependency> entries that appear *outside*
    <dependencyManagement>."""
    # Strip dependencyManagement block first
    cleaned = re.sub(
        r"<dependencyManagement>[\s\S]*?</dependencyManagement>", "", pom_text
    )
    deps: list[dict[str, str]] = []
    for m in re.finditer(r"<dependency>([\s\S]*?)</dependency>", cleaned):
        body = m.group(1)
        gid = re.search(r"<groupId>([^<]+)</groupId>", body)
        aid = re.search(r"<artifactId>([^<]+)</artifactId>", body)
        ver = re.search(r"<version>([^<]+)</version>", body)
        sc = re.search(r"<scope>([^<]+)</scope>", body)
        opt = re.search(r"<optional>([^<]+)</optional>", body)
        if gid and aid:
            deps.append({
                "groupId": gid.group(1),
                "artifactId": aid.group(1),
                "version": ver.group(1) if ver else None,
                "scope": sc.group(1) if sc else None,
                "optional": opt.group(1) if opt else None,
            })
    return deps


# Dependencies whose pinned version in legacy services would clobber the
# version Spring Boot 3.2.2's BOM provides — drop the version pin and let
# the union/BOM win. (We still lift the dependency itself.)
DROP_VERSION_ON_LIFT = {
    ("org.springframework.kafka", "spring-kafka"),
    ("org.springframework", "spring-context"),
    ("org.springframework", "spring-core"),
    ("org.springframework", "spring-web"),
}

# Dependencies that dristi-common already declares with compile scope —
# they're transitively available, so don't lift again. NB: optional
# dependencies (lombok) are NOT transitive, so they must be lifted.
SUPPRESS_DEPS_FROM_LIFT = {
    ("org.springframework.boot", "spring-boot-starter-web"),
    ("org.springframework.boot", "spring-boot-starter-validation"),
    ("org.springframework.kafka", "spring-kafka"),
    ("com.fasterxml.jackson.core", "jackson-databind"),
    ("com.fasterxml.jackson.datatype", "jackson-datatype-jsr310"),
    ("org.apache.commons", "commons-lang3"),
    ("io.swagger.core.v3", "swagger-annotations"),
    ("net.minidev", "json-smart"),
    ("org.egov.services", "tracer"),
    ("org.egov", "mdms-client"),
}


def phase_8_wire_module_deps(manifest: dict) -> bool:
    """Add dristi-common + lift the source service's <dependency> entries
    into the target domain module POM, deduplicating against deps already
    present and against centrally-managed deps."""
    pom = MONOLITH_ROOT / f"domain-{manifest['target_module']}" / "pom.xml"
    if not pom.exists():
        print(f"Phase 8: {pom} missing")
        return False
    pom_text = pom.read_text(encoding="utf-8")

    # Existing module deps
    existing_keys = {(d["groupId"], d["artifactId"]) for d in _parse_pom_deps(pom_text)}

    # Source service deps
    source_pom = REPO_ROOT / manifest["source_dir"] / "pom.xml"
    if not source_pom.exists():
        print(f"Phase 8: source POM {source_pom} missing — only adding dristi-common")
        new_deps: list[dict[str, str]] = []
    else:
        source_deps = _parse_pom_deps(source_pom.read_text(encoding="utf-8"))
        new_deps = []
        for d in source_deps:
            key = (d["groupId"], d["artifactId"])
            if key in existing_keys or key in SUPPRESS_DEPS_FROM_LIFT:
                continue
            if key in DROP_VERSION_ON_LIFT:
                d = {**d, "version": None}
            new_deps.append(d)
            existing_keys.add(key)

    # Always ensure dristi-common dep
    common_key = ("org.pucar.dristi", "dristi-common")
    if common_key not in existing_keys:
        new_deps.insert(0, {
            "groupId": "org.pucar.dristi",
            "artifactId": "dristi-common",
            "version": "${project.version}",
            "scope": None,
            "optional": None,
        })

    if not new_deps:
        print("Phase 8: nothing new to wire (module POM already covers everything)")
        return False

    block = "\n".join(
        _format_dep(d["groupId"], d["artifactId"], d["version"], d["scope"], d["optional"])
        for d in new_deps
    )
    new_pom = pom_text.replace("    </dependencies>", block + "\n    </dependencies>")
    if new_pom == pom_text:
        print("Phase 8: could not locate </dependencies> in module POM")
        return False
    pom.write_text(new_pom, encoding="utf-8")
    print(f"Phase 8: lifted {len(new_deps)} deps into {pom.relative_to(REPO_ROOT)}")
    for d in new_deps:
        version = f":{d['version']}" if d["version"] else ""
        print(f"  - {d['groupId']}:{d['artifactId']}{version}")
    return True


# --- driver -----------------------------------------------------------------


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--service", required=True)
    parser.add_argument("--module", required=True, help="case-lifecycle | identity-access | integration | payments")
    parser.add_argument("--subdomain", required=True)
    parser.add_argument("--phase", default="1,2,3,4,5,6,7,8",
                        help="comma-separated subset of phases to run")
    args = parser.parse_args()

    phases = {int(p) for p in args.phase.split(",") if p.strip()}
    ensure_jdk_17()
    service_dir = find_service_dir(args.service)

    manifest = phase_1_analyze(args, service_dir)

    target_pkg = manifest["target_package"]
    target_dir = (
        MONOLITH_ROOT
        / f"domain-{manifest['target_module']}"
        / "src"
        / "main"
        / "java"
        / target_pkg.replace(".", "/")
        / "internal"
    )

    if 2 in phases:
        phase_2_scaffold(manifest)
    if 3 in phases:
        phase_3_auto_rename(manifest, target_dir)
    # Phase 6 (test migration) runs BEFORE Phase 4 so the test tree exists
    # when Phase 4's auto-import sweep walks both main + test directories.
    if 6 in phases:
        phase_6_test_migration(manifest)
    if 4 in phases:
        phase_4_deduplicate(manifest, target_dir)
    if 5 in phases:
        phase_5_detect_rest(manifest, target_dir)
    if 8 in phases:
        phase_8_wire_module_deps(manifest)
    if 7 in phases:
        nfail, _ = phase_7_validate(manifest, target_dir)
        if nfail:
            return 1
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
