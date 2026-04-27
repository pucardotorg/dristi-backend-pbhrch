#!/usr/bin/env python3
"""
Pipeline 2 / Phase 6 — Fix unresolved imports in canonical classes so that
dristi-common compiles in isolation.

What this rewrites in each canonical .java file under
dristi-monolith/dristi-common/src/main/java/org/pucar/dristi/common/:
  1. ServiceRequestRepository:
     digit.repository.ServiceRequestRepository
     org.pucar.dristi.repository.ServiceRequestRepository
       -> org.pucar.dristi.common.repository.ServiceRequestRepository

  2. Configuration -> CommonConfiguration:
     - import line rewritten
     - all in-source `Configuration` type references rewritten to
       `CommonConfiguration` (the field is usually `private Configuration configs`)

  3. ServiceConstants (static imports only) -> CommonConstants:
     import static digit.config.ServiceConstants.*
     import static org.pucar.dristi.config.ServiceConstants.*
     import static digit.config.ServiceConstants.SOME_CONST
       -> import static org.pucar.dristi.common.config.CommonConstants.<*|SOME_CONST>

IndividualUtil uses service-specific models (Individual, IndividualSearchRequest)
that are not yet in dristi-common; those imports remain unresolved and a
TODO comment is added at the top.
"""

from __future__ import annotations

import re
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parents[3]
COMMON_ROOT = REPO_ROOT / "dristi-monolith" / "dristi-common" / "src" / "main" / "java" / "org" / "pucar" / "dristi" / "common"

# (regex, replacement)
SUBSTITUTIONS: list[tuple[re.Pattern, str]] = [
    # ServiceRequestRepository — both old paths
    (
        re.compile(r"^(\s*import\s+)(?:digit|org\.pucar\.dristi)\.repository\.ServiceRequestRepository\s*;", re.MULTILINE),
        r"\1org.pucar.dristi.common.repository.ServiceRequestRepository;",
    ),
    # Configuration -> CommonConfiguration import
    (
        re.compile(r"^(\s*import\s+)(?:digit|org\.pucar\.dristi)\.config\.Configuration\s*;", re.MULTILINE),
        r"\1org.pucar.dristi.common.config.CommonConfiguration;",
    ),
    # ServiceConstants static imports (wildcard or specific) -> CommonConstants
    (
        re.compile(r"^(\s*import\s+static\s+)(?:digit|org\.pucar\.dristi)\.config\.ServiceConstants\.", re.MULTILINE),
        r"\1org.pucar.dristi.common.config.CommonConstants.",
    ),
]

# For type-name replacement we walk word-boundaries so we don't munge things
# named ConfigurationXXX or related.
CONFIGURATION_TYPE = re.compile(r"\bConfiguration\b")


def fix_file(path: Path) -> tuple[bool, list[str]]:
    """Apply substitutions; return (changed, list_of_what_was_done)."""
    text = path.read_text(encoding="utf-8")
    new_text = text
    notes: list[str] = []

    # Run import-level substitutions
    for pattern, repl in SUBSTITUTIONS:
        new, n = pattern.subn(repl, new_text)
        if n:
            notes.append(f"{n}× {pattern.pattern[:60]}...")
            new_text = new

    # If we replaced the Configuration import, also rename the type usage
    if "org.pucar.dristi.common.config.CommonConfiguration" in new_text:
        renamed = CONFIGURATION_TYPE.sub("CommonConfiguration", new_text)
        if renamed != new_text:
            n = len(CONFIGURATION_TYPE.findall(new_text))
            notes.append(f"{n}× Configuration -> CommonConfiguration")
            new_text = renamed

    if new_text != text:
        path.write_text(new_text, encoding="utf-8")
        return True, notes
    return False, notes


def main() -> int:
    if not COMMON_ROOT.exists():
        print(f"ERROR: {COMMON_ROOT} not found", file=sys.stderr)  # type: ignore[name-defined]
        return 1

    changed_count = 0
    for java in sorted(COMMON_ROOT.rglob("*.java")):
        # Skip the new common config files themselves
        if java.parent.name == "config":
            continue
        changed, notes = fix_file(java)
        rel = java.relative_to(REPO_ROOT)
        if changed:
            changed_count += 1
            print(f"✓ {rel}")
            for n in notes:
                print(f"    {n}")
        else:
            print(f"- {rel} (no changes)")

    print()
    print(f"Updated {changed_count} canonical files")
    return 0


if __name__ == "__main__":
    import sys
    raise SystemExit(main())
