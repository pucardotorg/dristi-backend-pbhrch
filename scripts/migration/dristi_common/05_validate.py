#!/usr/bin/env python3
"""
Pipeline 2 / Phase 5 — VALIDATE Phase 4a output.

Gates:
  1. Every entry in phase4a_deleted.csv no longer exists on disk.
  2. Every entry in phase4a_divergent.csv DOES still exist on disk.
  3. No remaining duplicate (in any service) hashes equal to the canonical hash.
  4. Canonical files in dristi-monolith/dristi-common/ still exist.
  5. dristi-monolith parent + modules pass `mvn validate`.

Exits non-zero if any gate fails.
"""

from __future__ import annotations

import csv
import hashlib
import re
import subprocess
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parents[3]
OUT_DIR = Path(__file__).resolve().parent / "output"
PICKS = OUT_DIR / "dristi_common_canonical_picks.csv"
INVENTORY = OUT_DIR / "dristi_common_inventory.csv"
DELETED = OUT_DIR / "phase4a_deleted.csv"
DIVERGENT = OUT_DIR / "phase4a_divergent.csv"
COMMON_ROOT = REPO_ROOT / "dristi-monolith" / "dristi-common" / "src" / "main" / "java"

PACKAGE_RE = re.compile(r"^\s*package\s+([\w.]+)\s*;", re.MULTILINE)
IMPORT_LINE_RE = re.compile(r"^\s*import\s+[^;]+;\s*$", re.MULTILINE)
LINE_COMMENT_RE = re.compile(r"//[^\n]*")
BLOCK_COMMENT_RE = re.compile(r"/\*.*?\*/", re.DOTALL)
WS_RE = re.compile(r"\s+")

SUBPKG_BY_CLASS = {
    "MdmsUtil": "util", "IdgenUtil": "util", "WorkflowUtil": "util",
    "FileStoreUtil": "util", "UserUtil": "util", "UrlShortenerUtil": "util",
    "IndividualUtil": "util", "Producer": "kafka",
    "ServiceRequestRepository": "repository",
    "AuditDetails": "models", "ResponseInfo": "models",
}

# Same exclusion as Phase 1 — these services are out of scope and not modified.
DESCOPED = {"ocr-service", "sunbirdrc-credential-service", "artifacts"}


def hash_normalized(text: str) -> str:
    stripped = PACKAGE_RE.sub("", text)
    stripped = IMPORT_LINE_RE.sub("", stripped)
    stripped = BLOCK_COMMENT_RE.sub("", stripped)
    stripped = LINE_COMMENT_RE.sub("", stripped)
    stripped = WS_RE.sub(" ", stripped).strip()
    return hashlib.md5(stripped.encode("utf-8")).hexdigest()


def load_csv(path: Path) -> list[dict[str, str]]:
    with path.open(encoding="utf-8") as fh:
        return list(csv.DictReader(fh))


def main() -> int:
    failures: list[str] = []

    # Gate 1: deleted files are gone
    deleted = load_csv(DELETED)
    still_present = [r for r in deleted if (REPO_ROOT / r["path"]).exists()]
    if still_present:
        failures.append(f"Gate 1: {len(still_present)} files in phase4a_deleted.csv still exist")
        for r in still_present[:5]:
            failures.append(f"  - {r['path']}")
    else:
        print(f"PASS Gate 1: all {len(deleted)} files deleted")

    # Gate 2: divergent files still exist
    divergent = load_csv(DIVERGENT)
    missing = [r for r in divergent if not (REPO_ROOT / r["path"]).exists()]
    if missing:
        failures.append(f"Gate 2: {len(missing)} divergent files unexpectedly missing")
        for r in missing[:5]:
            failures.append(f"  - {r['path']}")
    else:
        print(f"PASS Gate 2: all {len(divergent)} divergent files preserved")

    # Gate 3: canonical-hash dups are zero in services
    picks = {row["class"]: row for row in load_csv(PICKS)}
    inventory = load_csv(INVENTORY)
    canonical_hash: dict[str, str] = {}
    for cls, pick in picks.items():
        target = pick["canonical_path"]
        for r in inventory:
            if r["class"] == cls and r["path"] == target:
                canonical_hash[cls] = r["content_hash"]
                break

    leaks: list[str] = []
    scan_roots = [REPO_ROOT / "dristi-services", REPO_ROOT / "integration-services"]
    for cls, expected_hash in canonical_hash.items():
        for root in scan_roots:
            if not root.exists():
                continue
            for java in root.rglob(f"{cls}.java"):
                # Skip de-scoped services (Phase 1 also skipped them)
                try:
                    svc = java.relative_to(root).parts[0]
                except (ValueError, IndexError):
                    continue
                if svc in DESCOPED:
                    continue
                try:
                    text = java.read_text(encoding="utf-8")
                except (OSError, UnicodeDecodeError):
                    continue
                # only treat it as a duplicate if it actually defines the class
                if not re.search(rf"\bclass\s+{re.escape(cls)}\b", text):
                    continue
                if hash_normalized(text) == expected_hash:
                    leaks.append(f"{cls}: {java.relative_to(REPO_ROOT)}")

    if leaks:
        failures.append(f"Gate 3: {len(leaks)} canonical-hash duplicates remain in services")
        for leak in leaks[:5]:
            failures.append(f"  - {leak}")
    else:
        print(f"PASS Gate 3: no canonical-hash duplicates remain in services")

    # Gate 4: canonical files exist in dristi-common
    missing_canonical: list[str] = []
    for cls, sub in SUBPKG_BY_CLASS.items():
        path = COMMON_ROOT / "org" / "pucar" / "dristi" / "common" / sub / f"{cls}.java"
        if not path.exists():
            missing_canonical.append(str(path.relative_to(REPO_ROOT)))
    if missing_canonical:
        failures.append(f"Gate 4: {len(missing_canonical)} canonical files missing")
        for p in missing_canonical:
            failures.append(f"  - {p}")
    else:
        print(f"PASS Gate 4: all {len(SUBPKG_BY_CLASS)} canonical files present")

    # Gate 5: mvn validate
    print("Running `mvn -B -q validate` on dristi-monolith ...")
    try:
        subprocess.run(
            ["mvn", "-B", "-q", "validate"],
            cwd=REPO_ROOT / "dristi-monolith",
            check=True,
        )
        print("PASS Gate 5: mvn validate clean")
    except subprocess.CalledProcessError as exc:
        failures.append(f"Gate 5: mvn validate failed (exit {exc.returncode})")

    print()
    if failures:
        print("FAIL — Phase 4a gates:")
        for f in failures:
            print(f"  {f}")
        return 1
    print("ALL GATES PASS")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
