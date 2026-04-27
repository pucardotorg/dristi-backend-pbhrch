#!/usr/bin/env python3
"""
Pipeline 2 / Phase 2 — CLASSIFY each protected class.

Reads build/dristi_common_inventory.csv. For each class:
  A  — single content variant (all copies byte-identical sans package/imports)
  A* — small variants (2-3 unique), one variant dominates. Phase 3 picks the
       majority and logs the divergent variants for review.
  B  — many variants (4+ unique). Phase 3 still picks majority but flags as
       "needs-manual-review" — a human should diff before Phase 4 deletion.

Output:
  - build/dristi_common_classification.csv   (per-class summary)
  - build/dristi_common_canonical_picks.csv  (which file to use as canonical)
  - build/dristi_common_review_needed.txt    (human-readable diff guide)
"""

from __future__ import annotations

import csv
import sys
from collections import Counter, defaultdict
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parents[3]
OUT_DIR = Path(__file__).resolve().parent / "output"
INVENTORY = OUT_DIR / "dristi_common_inventory.csv"
CLASSIFICATION = OUT_DIR / "dristi_common_classification.csv"
PICKS = OUT_DIR / "dristi_common_canonical_picks.csv"
REVIEW = OUT_DIR / "dristi_common_review_needed.txt"

# Preferred sources: when picking the majority variant fails to break a tie,
# prefer files from these services (they tend to have the most up-to-date
# implementations per repo conventions).
PREFERRED_SERVICES = ["case", "hearing", "advocate", "application"]


def classify_unique_count(unique: int) -> str:
    if unique == 1:
        return "A"
    if unique <= 3:
        return "A*"
    return "B"


def pick_canonical(rows: list[dict[str, str]]) -> tuple[dict[str, str], Counter]:
    """Choose the canonical row for a class.
    Strategy: most common content_hash wins. Ties broken by PREFERRED_SERVICES,
    then alphabetical service name."""
    hash_counter = Counter(r["content_hash"] for r in rows)
    most_common_hash = hash_counter.most_common(1)[0][0]
    candidates = [r for r in rows if r["content_hash"] == most_common_hash]

    def sort_key(row: dict[str, str]) -> tuple:
        try:
            pref_idx = PREFERRED_SERVICES.index(row["service"])
        except ValueError:
            pref_idx = len(PREFERRED_SERVICES)
        return (pref_idx, row["service"])

    candidates.sort(key=sort_key)
    return candidates[0], hash_counter


def main() -> int:
    if not INVENTORY.exists():
        print(f"ERROR: run Phase 1 first to generate {INVENTORY}", file=sys.stderr)
        return 1

    rows = list(csv.DictReader(INVENTORY.open(encoding="utf-8")))
    by_class: dict[str, list[dict[str, str]]] = defaultdict(list)
    for r in rows:
        by_class[r["class"]].append(r)

    classification_rows: list[dict[str, str]] = []
    pick_rows: list[dict[str, str]] = []
    review_lines: list[str] = []

    for cls, cls_rows in sorted(by_class.items()):
        unique = len({r["content_hash"] for r in cls_rows})
        category = classify_unique_count(unique)
        canonical, hash_counter = pick_canonical(cls_rows)
        majority_pct = round(hash_counter[canonical["content_hash"]] / len(cls_rows) * 100)

        classification_rows.append(
            {
                "class": cls,
                "copies": str(len(cls_rows)),
                "unique_variants": str(unique),
                "category": category,
                "majority_pct": str(majority_pct),
            }
        )
        pick_rows.append(
            {
                "class": cls,
                "category": category,
                "canonical_path": canonical["path"],
                "canonical_service": canonical["service"],
                "canonical_package": canonical["package"],
            }
        )

        if category in ("A*", "B"):
            review_lines.append(f"=== {cls} ({category}, {unique} variants, {len(cls_rows)} copies) ===")
            review_lines.append(f"  CANONICAL PICK: {canonical['path']}")
            review_lines.append(f"    (matches {hash_counter[canonical['content_hash']]}/{len(cls_rows)} copies = {majority_pct}%)")
            review_lines.append("")
            review_lines.append("  Other variants (review before deletion):")
            other_variants: dict[str, list[str]] = defaultdict(list)
            for r in cls_rows:
                if r["content_hash"] != canonical["content_hash"]:
                    other_variants[r["content_hash"]].append(r["path"])
            for h, paths in sorted(other_variants.items(), key=lambda kv: -len(kv[1])):
                review_lines.append(f"    variant {h[:8]} ({len(paths)} copies):")
                for p in paths:
                    review_lines.append(f"      - {p}")
            review_lines.append("")

    CLASSIFICATION.parent.mkdir(parents=True, exist_ok=True)
    with CLASSIFICATION.open("w", newline="", encoding="utf-8") as fh:
        writer = csv.DictWriter(
            fh, fieldnames=["class", "copies", "unique_variants", "category", "majority_pct"]
        )
        writer.writeheader()
        writer.writerows(classification_rows)

    with PICKS.open("w", newline="", encoding="utf-8") as fh:
        writer = csv.DictWriter(
            fh, fieldnames=["class", "category", "canonical_path", "canonical_service", "canonical_package"]
        )
        writer.writeheader()
        writer.writerows(pick_rows)

    REVIEW.write_text("\n".join(review_lines) + "\n", encoding="utf-8")

    print(f"Wrote {CLASSIFICATION.relative_to(REPO_ROOT)}")
    print(f"Wrote {PICKS.relative_to(REPO_ROOT)}")
    print(f"Wrote {REVIEW.relative_to(REPO_ROOT)}")
    print()
    print(f"  {'class':<28} {'copies':>6} {'unique':>6} {'cat':>4} {'majority':>9}")
    for row in classification_rows:
        print(
            f"  {row['class']:<28} {row['copies']:>6} {row['unique_variants']:>6} "
            f"{row['category']:>4} {row['majority_pct'] + '%':>9}"
        )
    needs_review = sum(1 for r in classification_rows if r["category"] in ("A*", "B"))
    print()
    print(f"Classes needing review before Phase 4: {needs_review}/{len(classification_rows)}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
