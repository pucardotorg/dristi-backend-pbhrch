#!/usr/bin/env python3
"""Shared library for dead-`@Value`-key detection and registration.

A *dead key* is a `@Value("${X}")` property that was bound in some
`Configuration.java` at the branch base (`monolith/main`) but is no
longer bound at `HEAD` — typically because the field that read it was
deleted as part of a REST→direct conversion (Rule 32 + Rule 37).

If a dead key isn't registered in either
  - `SERVICE_DEAD_KEYS` (manual, in `run_consolidation.py`), or
  - `auto_dead_keys.json` (auto, written by Phase 96),
then the next `run_consolidation.py` will mechanically re-add it to the
subdomain's `application-<subdomain>.yml`, silently undoing the
operator's Rule 37 sweep.

Three consumers import from this module:
  - `per_module/run_module_migration.py` Phase 95 (detect) + 96 (register)
  - `gates/dead_keys_gate.py`           (Layer 2 enforcement)
  - `config_consolidation/run_consolidation.py` (union sidecar on regen)

Per **Rule 42** (`PIPELINE_RULES.md`).
"""

from __future__ import annotations

import json
import re
import subprocess
from pathlib import Path

# scripts/migration/dead_keys_lib.py → parents[2] = repo root
REPO_ROOT = Path(__file__).resolve().parents[2]
SIDECAR = (
    REPO_ROOT
    / "scripts"
    / "migration"
    / "config_consolidation"
    / "auto_dead_keys.json"
)

# Subdomain (the path segment under
# `domain-<module>/src/main/java/<base-pkg>/<subdomain>/`) → source service
# name (the `--service` value passed to `run_consolidation.py`, matching
# `dristi-services/<svc>/`). Derived from SERVICE_REGISTRY.md.
#
# Edge case: when a subdomain absorbs multiple services (e.g.
# `esign` ← `e-sign-svc` + `esign-interceptor`), the entry maps to the
# primary; the secondary's keys stay hand-curated in the
# `SERVICE_DEAD_KEYS` literal of `run_consolidation.py`.
SUBDOMAIN_TO_SERVICE: dict[str, str] = {
    # domain-case-lifecycle
    "locksvc": "lock-svc",
    "cases": "case",
    "hearing": "hearing",
    "order": "order",
    "task": "task",
    "evidence": "evidence",
    "application": "application",
    "casemanagement": "casemanagement",
    "hearingmanagement": "hearing-management",
    "ordermanagement": "order-management",
    "taskmanagement": "task-management",
    "analytics": "analytics",
    "bailbond": "bail-bond",
    "notification": "Notification",
    "digitalizeddocuments": "digitalized-documents",
    "ctc": "ctc",
    "templateconfiguration": "template-configuration",
    "abdiary": "ab-diary",
    "inportalsurvey": "inportal-survey",
    "scheduler": "scheduler-svc",
    "openapi": "openapi",
    # domain-identity-access
    "advocate": "advocate",
    "advocateoffice": "advocate-office-management",
    # domain-integration
    "summons": "summons-svc",
    "treasury": "treasury-backend",
    "njdg": "njdg-transformer",
    "icops": "icops_integration-kerala",
    "esign": "e-sign-svc",
    "epost": "epost-tracker",
    "bank": "bank-details",
    # domain-payments
    "calculator": "payment-calculator-svc",
}

# Base packages Phase 1 places subdomains under. Used to extract subdomain
# from a Configuration.java path.
DOMAIN_BASE_PACKAGES = (
    "caselifecycle",
    "identityaccess",
    "integration",
    "payments",
)

# Matches single-line `@Value("${KEY}")` and `@Value("${KEY:default}")`.
# Captures KEY only (stops at `:`, `}`, or whitespace).
_VALUE_RE = re.compile(r'@Value\(\s*"\$\{([^}:\s]+)')

# Any `${KEY}` placeholder anywhere in the source — catches
# `@KafkaListener(topics = "${X}")`, `@Scheduled(cron = "${X}")`,
# `@KafkaListener(topics = {"${X}", "${Y}"})`, multi-line, etc.
# Used for the *cur* side of the diff so a key that *moved* from
# `@Value` to a listener / cron annotation is still recognised as
# alive (the casemanagement #101 / #111 regression).
_PLACEHOLDER_RE = re.compile(r'\$\{([^}:\s]+)')

# `@ConditionalOnProperty(name = "X" | value = "X" | prefix = "X")` —
# bare bean-condition key without `${}` wrapping. Extract the
# annotation block first, then pull every `name|value|prefix = "X"`
# attribute inside (a single annotation can carry multiple).
_CONDITIONAL_ON_PROPERTY_BLOCK_RE = re.compile(
    r'@ConditionalOnProperty\s*\(([^)]*)\)', re.DOTALL
)
_PROPERTY_ATTR_RE = re.compile(r'(?:name|value|prefix)\s*=\s*"([^"\s]+)')

# `env.getProperty("X")` / `environment.getRequiredProperty("X")` — same
# bare-key form Spring lets you read at runtime without an annotation.
_ENV_GET_RE = re.compile(
    r'(?:env|environment)\.(?:getProperty|getRequiredProperty)\(\s*"([^"\s]+)'
)


def parse_value_keys(text: str) -> set[str]:
    """Every `${KEY}` referenced by an `@Value(...)` annotation in `text`.

    Used for the *pre* side of the diff: "what was bound via `@Value`
    before the operator's REST→direct sweep." A key that disappears
    from this set is a candidate dead key — provided it also doesn't
    appear in any other reference form (see `parse_all_key_references`).
    """
    return set(_VALUE_RE.findall(text))


def parse_all_key_references(text: str) -> set[str]:
    """Every property key referenced anywhere in `text` — `@Value`,
    `@KafkaListener(topics = …)`, `@Scheduled(cron = …)`, any other
    `${X}` placeholder, plus `@ConditionalOnProperty(name = …)` and
    `env.getProperty("…")`.

    Used for the *cur* side of the diff so the detector treats a key
    that moved from `@Value` to a listener annotation as still alive.
    Without this, dropping the `@Value` field while keeping the
    `@KafkaListener(topics = "${X}")` registers `X` as dead — and the
    next consolidation regen yanks `X` out of the per-service yml,
    crashing the listener at startup. See PR #111 regression on PR #101
    (casemanagement) for the live example this guard prevents.
    """
    keys = set(_PLACEHOLDER_RE.findall(text))
    for block in _CONDITIONAL_ON_PROPERTY_BLOCK_RE.findall(text):
        keys.update(_PROPERTY_ATTR_RE.findall(block))
    keys.update(_ENV_GET_RE.findall(text))
    return keys


def subdomain_from_path(path: Path) -> str | None:
    """Extract `<subdomain>` from a path like
    `dristi-monolith/domain-<m>/src/main/java/<pkg-base>/<subdomain>/...`.

    Returns None if no recognised base package is in the path.
    """
    parts = path.parts
    for marker in DOMAIN_BASE_PACKAGES:
        if marker in parts:
            i = parts.index(marker)
            if i + 1 < len(parts):
                return parts[i + 1]
    return None


def _run_git(args: list[str]) -> str:
    return subprocess.check_output(
        ["git", "-C", str(REPO_ROOT), *args],
        text=True,
        stderr=subprocess.DEVNULL,
    )


def branch_base(against: str = "origin/monolith/main") -> str:
    """Merge-base commit between `HEAD` and `against`.

    Falls back to the local `monolith/main` ref if `origin/...` isn't
    available (offline / before first fetch).
    """
    for ref in (against, "monolith/main"):
        try:
            return _run_git(["merge-base", ref, "HEAD"]).strip()
        except subprocess.CalledProcessError:
            continue
    raise RuntimeError(
        "Could not determine branch base. "
        "Tried 'origin/monolith/main' and 'monolith/main'."
    )


def git_diff_configurations(base: str, head: str = "HEAD") -> list[Path]:
    """Absolute paths of every `Configuration.java` modified between
    `base` and `head`, including deletions (the file may not exist on
    disk anymore).
    """
    out = _run_git(
        ["diff", "--name-only", f"{base}..{head}", "--", "*Configuration.java"]
    )
    return [REPO_ROOT / line.strip() for line in out.splitlines() if line.strip()]


def git_show(base: str, path: Path) -> str:
    """Contents of `path` at `base`. Empty string if it didn't exist."""
    rel = path.relative_to(REPO_ROOT)
    try:
        return _run_git(["show", f"{base}:{rel.as_posix()}"])
    except subprocess.CalledProcessError:
        return ""


def detect_dead_keys(base: str | None = None) -> list[tuple[str, str, Path]]:
    """Detect every `@Value("${X}")` removed from any `Configuration.java`
    between `base` (default: merge-base with `origin/monolith/main`) and
    `HEAD`.

    Returns `(source_service, property_key, configuration_path)` tuples.
    Each removed key in a single file produces its own tuple.
    """
    if base is None:
        base = branch_base()
    findings: list[tuple[str, str, Path]] = []
    for cfg in git_diff_configurations(base):
        pre_text = git_show(base, cfg)
        cur_text = cfg.read_text(encoding="utf-8") if cfg.exists() else ""
        # `pre` is restricted to @Value bindings — the cleanup pattern
        # this gate targets (Rule 37 sweeps following REST→direct).
        # `cur` is the union of every reference form (Rule 42, refined
        # by PR #111): if a key moved from @Value to @KafkaListener /
        # @Scheduled / @ConditionalOnProperty, it's still alive and
        # must NOT be registered as dead.
        removed = parse_value_keys(pre_text) - parse_all_key_references(cur_text)
        if not removed:
            continue
        subdomain = subdomain_from_path(cfg)
        if subdomain is None:
            continue
        svc = SUBDOMAIN_TO_SERVICE.get(subdomain)
        if svc is None:
            # Unknown subdomain — surface but don't crash. Operator
            # extends SUBDOMAIN_TO_SERVICE when a new subdomain appears.
            print(
                f"WARN: dead_keys_lib: unknown subdomain {subdomain!r} "
                f"(from {cfg.relative_to(REPO_ROOT)}) — skipping"
            )
            continue
        for key in sorted(removed):
            findings.append((svc, key, cfg))
    return findings


def load_auto_dead_keys() -> dict[str, set[str]]:
    """Read `auto_dead_keys.json` → `{service: set(keys)}`. Empty if the
    sidecar is missing or empty.
    """
    if not SIDECAR.exists():
        return {}
    raw_text = SIDECAR.read_text(encoding="utf-8").strip()
    if not raw_text:
        return {}
    raw = json.loads(raw_text)
    return {svc: set(entry.get("keys", [])) for svc, entry in raw.items()}


def write_auto_dead_keys(
    findings: list[tuple[str, str, Path]], commit: str
) -> None:
    """Idempotently splice `(svc, key)` pairs into `auto_dead_keys.json`.

    Each per-service entry tracks the union of keys plus the list of
    commits that contributed (audit trail). Re-runs of the same commit
    are a no-op.
    """
    data: dict = {}
    if SIDECAR.exists():
        raw_text = SIDECAR.read_text(encoding="utf-8").strip()
        if raw_text:
            data = json.loads(raw_text)
    for svc, key, _ in findings:
        entry = data.setdefault(svc, {"keys": [], "commits": []})
        if key not in entry["keys"]:
            entry["keys"].append(key)
        if commit and commit not in entry["commits"]:
            entry["commits"].append(commit)
    # Sort for deterministic output (no diff churn on re-runs).
    for entry in data.values():
        entry["keys"] = sorted(entry["keys"])
        entry["commits"] = sorted(entry["commits"])
    SIDECAR.parent.mkdir(parents=True, exist_ok=True)
    SIDECAR.write_text(
        json.dumps(data, indent=2, sort_keys=True) + "\n", encoding="utf-8"
    )


def head_short() -> str:
    """Short HEAD commit hash, or empty string if not in a git checkout."""
    try:
        return _run_git(["rev-parse", "--short", "HEAD"]).strip()
    except subprocess.CalledProcessError:
        return ""
