#!/usr/bin/env python3
"""Dead-keys CI gate (Rule 42, Layer 2).

Independent of `/migrate-service`. Re-runs the
`dead_keys_lib.detect_dead_keys()` projection against the current branch
and asserts every dropped `@Value("${X}")` key is registered — either in
`run_consolidation.py`'s hand-maintained `SERVICE_DEAD_KEYS` literal, or
in `config_consolidation/auto_dead_keys.json` (the sidecar written by
Phase 96).

Exit code:
  0 — no drops detected, or every drop is registered
  1 — at least one dropped key is unregistered

Wire from CI alongside `BeanNameCollisionTest` / `ModuleStructureTest`.
If CI isn't yet configured, run as an audit step from
`/migrate-service` Step 3.3.5 before maven verification.

Usage:
  python3 scripts/migration/gates/dead_keys_gate.py [--base <ref>]
"""

from __future__ import annotations

import argparse
import sys
from pathlib import Path

# Allow `from dead_keys_lib import ...` and `from run_consolidation import ...`.
_MIGRATION_ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(_MIGRATION_ROOT))
sys.path.insert(0, str(_MIGRATION_ROOT / "config_consolidation"))

from dead_keys_lib import (  # noqa: E402
    detect_dead_keys,
    load_auto_dead_keys,
    REPO_ROOT,
)
from run_consolidation import SERVICE_DEAD_KEYS  # noqa: E402


def _registered_keys() -> dict[str, set[str]]:
    """Union of `SERVICE_DEAD_KEYS` (hand-maintained) and `auto_dead_keys.json`
    (auto-written by Phase 96). Same shape `run_consolidation.py` reads.
    """
    merged: dict[str, set[str]] = {
        svc: set(keys) for svc, keys in SERVICE_DEAD_KEYS.items()
    }
    for svc, keys in load_auto_dead_keys().items():
        merged.setdefault(svc, set()).update(keys)
    return merged


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    parser.add_argument(
        "--base",
        default=None,
        help="Branch base ref to diff against (default: merge-base with "
        "origin/monolith/main).",
    )
    args = parser.parse_args()

    findings = detect_dead_keys(base=args.base)
    if not findings:
        print("dead_keys_gate: no @Value drops detected — PASS")
        return 0

    registered = _registered_keys()
    unregistered = [
        (svc, key, path)
        for svc, key, path in findings
        if key not in registered.get(svc, set())
    ]
    if not unregistered:
        print(
            f"dead_keys_gate: all {len(findings)} dropped @Value key(s) "
            f"registered — PASS"
        )
        return 0

    print(
        f"dead_keys_gate: FAIL — {len(unregistered)} dead @Value key(s) "
        f"not registered\n"
    )
    for svc, key, path in unregistered:
        rel = path.relative_to(REPO_ROOT) if path.is_absolute() else path
        print(f"  service={svc!r:24} key={key!r:40} removed from {rel}")
    print()
    print("Fix:")
    print(
        "  python3 scripts/migration/per_module/run_module_migration.py \\"
    )
    print(
        "      --service <svc> --module <mod> --subdomain <sub> --phase 95,96"
    )
    print(
        "  Then commit `scripts/migration/config_consolidation/"
        "auto_dead_keys.json`."
    )
    return 1


if __name__ == "__main__":
    raise SystemExit(main())
