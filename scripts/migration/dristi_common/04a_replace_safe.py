#!/usr/bin/env python3
"""
Pipeline 2 / Phase 4a — SAFE REPLACE.

Deletes only those duplicate copies whose normalized content hash matches
the canonical pick, then rewrites imports in the OWNING service to point
at dristi-common. Divergent copies are left untouched and recorded in a
tracker file for human curation.

Inputs:
  - scripts/migration/dristi_common/output/dristi_common_inventory.csv
  - scripts/migration/dristi_common/output/dristi_common_canonical_picks.csv

Outputs:
  - scripts/migration/dristi_common/output/phase4a_deleted.csv
  - scripts/migration/dristi_common/output/phase4a_divergent.csv
  - scripts/migration/dristi_common/output/phase4a_imports_rewritten.csv

Use --dry-run to preview without touching any files.
"""

from __future__ import annotations

import argparse
import csv
import re
import sys
from collections import defaultdict
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parents[3]
OUT_DIR = Path(__file__).resolve().parent / "output"
INVENTORY = OUT_DIR / "dristi_common_inventory.csv"
PICKS = OUT_DIR / "dristi_common_canonical_picks.csv"
DELETED_OUT = OUT_DIR / "phase4a_deleted.csv"
DIVERGENT_OUT = OUT_DIR / "phase4a_divergent.csv"
REWRITES_OUT = OUT_DIR / "phase4a_imports_rewritten.csv"

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

SERVICE_ROOTS = ("dristi-services", "integration-services")


def load_csv(path: Path) -> list[dict[str, str]]:
    with path.open(encoding="utf-8") as fh:
        return list(csv.DictReader(fh))


def find_service_dir(service: str) -> Path | None:
    for root in SERVICE_ROOTS:
        candidate = REPO_ROOT / root / service
        if candidate.is_dir():
            return candidate
    return None


def rewrite_imports(text: str, rewrites: list[dict[str, str]]) -> tuple[str, list[str]]:
    """Apply each rewrite (old_pkg.class -> new_pkg.class) to file text.
    Returns (new_text, list_of_classes_actually_rewritten)."""
    new_text = text
    classes_hit: list[str] = []
    for rw in rewrites:
        cls = rw["class"]
        old_pkg = rw["old_pkg"]
        new_pkg = rw["new_pkg"]
        if not old_pkg:
            continue
        plain = re.compile(
            rf"^(\s*import\s+){re.escape(old_pkg)}\.{re.escape(cls)}\s*;",
            re.MULTILINE,
        )
        replaced, n_plain = plain.subn(rf"\1{new_pkg}.{cls};", new_text)
        static = re.compile(
            rf"^(\s*import\s+static\s+){re.escape(old_pkg)}\.{re.escape(cls)}\.",
            re.MULTILINE,
        )
        replaced, n_static = static.subn(rf"\1{new_pkg}.{cls}.", replaced)
        if n_plain + n_static:
            classes_hit.append(cls)
            new_text = replaced
    return new_text, classes_hit


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--dry-run", action="store_true", help="Plan only, no edits")
    args = parser.parse_args()

    if not INVENTORY.exists() or not PICKS.exists():
        print("ERROR: run Phases 1-3 first", file=sys.stderr)
        return 1

    inventory = load_csv(INVENTORY)
    picks = {row["class"]: row for row in load_csv(PICKS)}

    # Map class -> canonical hash by looking up the canonical_path in inventory
    canonical_hash: dict[str, str] = {}
    for cls, pick in picks.items():
        target = pick["canonical_path"]
        for r in inventory:
            if r["class"] == cls and r["path"] == target:
                canonical_hash[cls] = r["content_hash"]
                break
        else:
            print(f"WARN: canonical path {target} not found in inventory", file=sys.stderr)

    # Plan: deletes vs divergent
    deletes: list[dict[str, str]] = []
    divergent: list[dict[str, str]] = []
    for r in inventory:
        cls = r["class"]
        if cls not in canonical_hash:
            continue
        if r["content_hash"] == canonical_hash[cls]:
            deletes.append(r)
        else:
            divergent.append(r)

    # Per-service rewrite plan
    rewrites_by_service: dict[str, list[dict[str, str]]] = defaultdict(list)
    for r in deletes:
        cls = r["class"]
        sub = SUBPKG_BY_CLASS[cls]
        rewrites_by_service[r["service"]].append(
            {
                "class": cls,
                "old_pkg": r["package"],
                "new_pkg": f"{COMMON_PKG}.{sub}",
            }
        )
    # Dedup rewrites per service (multiple deletes of same class in one service possible? no — but old_pkg might differ)
    for svc, rewrites in rewrites_by_service.items():
        seen = set()
        deduped = []
        for rw in rewrites:
            key = (rw["class"], rw["old_pkg"], rw["new_pkg"])
            if key in seen:
                continue
            seen.add(key)
            deduped.append(rw)
        rewrites_by_service[svc] = deduped

    # Plan summary
    by_class_deletes = defaultdict(int)
    by_class_keeps = defaultdict(int)
    for r in deletes:
        by_class_deletes[r["class"]] += 1
    for r in divergent:
        by_class_keeps[r["class"]] += 1

    print(f"Mode: {'DRY-RUN' if args.dry_run else 'EXECUTE'}")
    print()
    print(f"  {'class':<28} {'delete':>7} {'keep-divergent':>15}")
    classes = sorted(set(by_class_deletes) | set(by_class_keeps))
    for cls in classes:
        print(
            f"  {cls:<28} {by_class_deletes.get(cls, 0):>7} {by_class_keeps.get(cls, 0):>15}"
        )
    total_d = sum(by_class_deletes.values())
    total_k = sum(by_class_keeps.values())
    print()
    print(f"  TOTAL: delete={total_d}, keep-divergent={total_k}")
    print(f"  Services with imports to rewrite: {len(rewrites_by_service)}")

    # Persist plan tracker outputs (always — useful to see plan even on dry-run)
    OUT_DIR.mkdir(parents=True, exist_ok=True)
    with DELETED_OUT.open("w", newline="", encoding="utf-8") as fh:
        w = csv.DictWriter(fh, fieldnames=["class", "service", "path", "package", "content_hash"])
        w.writeheader()
        w.writerows(deletes)
    with DIVERGENT_OUT.open("w", newline="", encoding="utf-8") as fh:
        w = csv.DictWriter(fh, fieldnames=["class", "service", "path", "package", "content_hash"])
        w.writeheader()
        w.writerows(divergent)
    print()
    print(f"Wrote plan: {DELETED_OUT.relative_to(REPO_ROOT)}")
    print(f"Wrote plan: {DIVERGENT_OUT.relative_to(REPO_ROOT)}")

    if args.dry_run:
        print()
        print("Dry-run complete — no files modified.")
        return 0

    # ----- Execute -----
    # Step 1: delete duplicate files
    deleted_paths: list[str] = []
    for r in deletes:
        full_path = REPO_ROOT / r["path"]
        if full_path.exists():
            full_path.unlink()
            deleted_paths.append(r["path"])

    # Step 2: rewrite imports in each affected service
    rewritten_rows: list[dict[str, str]] = []
    for service, rewrites in rewrites_by_service.items():
        svc_dir = find_service_dir(service)
        if svc_dir is None:
            print(f"WARN: service dir for {service} not found", file=sys.stderr)
            continue
        for java in svc_dir.rglob("*.java"):
            if not java.is_file():
                continue
            text = java.read_text(encoding="utf-8")
            new_text, classes_hit = rewrite_imports(text, rewrites)
            if classes_hit:
                java.write_text(new_text, encoding="utf-8")
                rewritten_rows.append(
                    {
                        "service": service,
                        "path": str(java.relative_to(REPO_ROOT)),
                        "classes": ",".join(classes_hit),
                    }
                )

    with REWRITES_OUT.open("w", newline="", encoding="utf-8") as fh:
        w = csv.DictWriter(fh, fieldnames=["service", "path", "classes"])
        w.writeheader()
        w.writerows(rewritten_rows)

    print()
    print(f"DELETED {len(deleted_paths)} duplicate files")
    print(f"REWROTE imports in {len(rewritten_rows)} java files across {len(rewrites_by_service)} services")
    print(f"Wrote: {REWRITES_OUT.relative_to(REPO_ROOT)}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
