#!/usr/bin/env python3
"""
Pipeline 2 / Phase 3 — BUILD CANONICAL classes in dristi-common.

Reads build/dristi_common_canonical_picks.csv and copies each picked file
into dristi-monolith/dristi-common/, rewriting the `package` declaration to
match the new location.

Per-class destination:
  *Util              -> common/util/
  Producer           -> common/kafka/
  ServiceRequestRepository -> common/repository/
  AuditDetails, ResponseInfo -> common/models/

NOTE: Phase 3 does NOT attempt to fix internal imports (e.g. references to
each service's ServiceConstants/Configuration). Those will surface as
unresolved imports — that is expected, and resolved as part of Phase 4 +
the per-module migration pipeline. The point of Phase 3 is to land canonical
sources in dristi-common so reviewers can compare against variants.
"""

from __future__ import annotations

import csv
import re
import sys
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parents[3]
PICKS_PATH = Path(__file__).resolve().parent / "output" / "dristi_common_canonical_picks.csv"
COMMON_ROOT = REPO_ROOT / "dristi-monolith" / "dristi-common" / "src" / "main" / "java"
COMMON_PKG = "org.pucar.dristi.common"

SUBPKG_BY_CLASS = {
    "MdmsUtil": "util",
    "IdgenUtil": "util",
    "WorkflowUtil": "util",
    "FileStoreUtil": "util",
    "UserUtil": "util",
    "UrlShortenerUtil": "util",
    "IndividualUtil": "util",
    "Producer": "kafka",
    "ServiceRequestRepository": "repository",
    "AuditDetails": "models",
    "ResponseInfo": "models",
}

PACKAGE_RE = re.compile(r"^\s*package\s+([\w.]+)\s*;", re.MULTILINE)


def write_canonical(class_name: str, source_path: Path, target_pkg: str) -> Path:
    text = source_path.read_text(encoding="utf-8")

    # Replace package line with the new common package
    new_text, n = PACKAGE_RE.subn(f"package {target_pkg};", text, count=1)
    if n == 0:
        # File had no package declaration — prepend one
        new_text = f"package {target_pkg};\n\n{text}"

    header = (
        "// AUTO-EXTRACTED INTO dristi-common BY scripts/migration/dristi_common/03_build_canonical.py\n"
        f"// Source: {source_path.relative_to(REPO_ROOT)}\n"
        "// NOTE: imports referencing service-internal classes (ServiceConstants,\n"
        "// Configuration, web.models.*) may need follow-up — see Phase 4.\n"
    )
    new_text = header + new_text

    dest_dir = COMMON_ROOT / target_pkg.replace(".", "/")
    dest_dir.mkdir(parents=True, exist_ok=True)
    dest_path = dest_dir / f"{class_name}.java"
    dest_path.write_text(new_text, encoding="utf-8")
    return dest_path


def main() -> int:
    if not PICKS_PATH.exists():
        print(f"ERROR: run Phase 2 first to generate {PICKS_PATH}", file=sys.stderr)
        return 1

    written: list[tuple[str, str, str]] = []  # (class, source, target)

    for row in csv.DictReader(PICKS_PATH.open(encoding="utf-8")):
        cls = row["class"]
        if cls not in SUBPKG_BY_CLASS:
            print(f"WARN: no subpackage mapping for {cls}, skipping", file=sys.stderr)
            continue
        sub_pkg = SUBPKG_BY_CLASS[cls]
        target_pkg = f"{COMMON_PKG}.{sub_pkg}"
        source = REPO_ROOT / row["canonical_path"]
        if not source.exists():
            print(f"WARN: canonical source missing: {source}", file=sys.stderr)
            continue

        dest = write_canonical(cls, source, target_pkg)
        written.append((cls, str(source.relative_to(REPO_ROOT)), str(dest.relative_to(REPO_ROOT))))

    # Remove any stale .gitkeep that's now alongside real classes
    for keep in COMMON_ROOT.rglob(".gitkeep"):
        if any(keep.parent.glob("*.java")):
            keep.unlink()

    print(f"Wrote {len(written)} canonical classes to dristi-common:")
    for cls, src, dst in written:
        print(f"  {cls:<28}  from {src}")
        print(f"  {'':<28}    -> {dst}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
