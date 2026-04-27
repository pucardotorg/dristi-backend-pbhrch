#!/usr/bin/env python3
"""
Pipeline 2 / Phase 1 — INVENTORY of duplicate utility/model classes.

Scans dristi-services/ and integration-services/ for every Java file that
defines one of the PROTECTED classes (the classes that should live in
dristi-common after extraction).

Output: build/dristi_common_inventory.csv
  columns: class,service,path,package,content_hash
  - content_hash is md5 of the file with `package` / `import` lines stripped,
    so byte-identical implementations across services collapse to one hash.
"""

from __future__ import annotations

import csv
import hashlib
import re
import sys
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parents[3]
OUT_DIR = Path(__file__).resolve().parent / "output"
OUT_PATH = OUT_DIR / "dristi_common_inventory.csv"

# De-scoped services excluded from migration entirely.
DESCOPED = {"ocr-service", "sunbirdrc-credential-service", "artifacts"}

# Classes that should be deduplicated into dristi-common. ProducerService,
# RequestInfo, and CommonConfiguration appear in agentic_workflow.md but
# have zero copies in this repo, so they're omitted.
PROTECTED = [
    "MdmsUtil",
    "IdgenUtil",
    "WorkflowUtil",
    "FileStoreUtil",
    "UserUtil",
    "UrlShortenerUtil",
    "IndividualUtil",
    "Producer",
    "ServiceRequestRepository",
    "AuditDetails",
    "ResponseInfo",
]

PACKAGE_RE = re.compile(r"^\s*package\s+([\w.]+)\s*;", re.MULTILINE)
IMPORT_LINE_RE = re.compile(r"^\s*import\s+[^;]+;\s*$", re.MULTILINE)
LINE_COMMENT_RE = re.compile(r"//[^\n]*")
BLOCK_COMMENT_RE = re.compile(r"/\*.*?\*/", re.DOTALL)
WS_RE = re.compile(r"\s+")


def find_class_definition(text: str, class_name: str) -> bool:
    """True iff this file declares (not just references) the given class."""
    # Handles `public class Foo`, `class Foo<T>`, `final class Foo extends Bar`
    pattern = re.compile(
        rf"\b(?:public\s+|final\s+|abstract\s+)*class\s+{re.escape(class_name)}\b"
    )
    return bool(pattern.search(text))


def hash_normalized(text: str) -> str:
    """Hash file content with package, imports, comments, and whitespace
    stripped — so two services that differ only in formatting collapse to
    the same hash."""
    stripped = PACKAGE_RE.sub("", text)
    stripped = IMPORT_LINE_RE.sub("", stripped)
    stripped = BLOCK_COMMENT_RE.sub("", stripped)
    stripped = LINE_COMMENT_RE.sub("", stripped)
    stripped = WS_RE.sub(" ", stripped).strip()
    return hashlib.md5(stripped.encode("utf-8")).hexdigest()


def package_of(text: str) -> str:
    m = PACKAGE_RE.search(text)
    return m.group(1) if m else ""


def service_of(path: Path, repo_root: Path) -> str:
    rel = path.relative_to(repo_root).parts
    # rel = ("dristi-services", "<svc>", ...)
    return rel[1] if len(rel) >= 2 else "?"


def main() -> int:
    if not REPO_ROOT.exists():
        print(f"ERROR: {REPO_ROOT} not found")
        return 1

    rows: list[dict[str, str]] = []
    scan_roots = [REPO_ROOT / "dristi-services", REPO_ROOT / "integration-services"]

    for root in scan_roots:
        if not root.exists():
            continue
        for java_path in root.rglob("*.java"):
            # Skip de-scoped services
            try:
                svc = java_path.relative_to(root).parts[0]
            except (ValueError, IndexError):
                continue
            if svc in DESCOPED:
                continue
            try:
                text = java_path.read_text(encoding="utf-8")
            except (UnicodeDecodeError, OSError) as exc:
                print(f"WARN: skipping {java_path}: {exc}", file=sys.stderr)
                continue

            stem = java_path.stem
            if stem not in PROTECTED:
                continue
            if not find_class_definition(text, stem):
                continue

            rows.append(
                {
                    "class": stem,
                    "service": svc,
                    "path": str(java_path.relative_to(REPO_ROOT)),
                    "package": package_of(text),
                    "content_hash": hash_normalized(text),
                }
            )

    rows.sort(key=lambda r: (r["class"], r["service"], r["path"]))

    OUT_PATH.parent.mkdir(parents=True, exist_ok=True)
    with OUT_PATH.open("w", newline="", encoding="utf-8") as fh:
        writer = csv.DictWriter(
            fh, fieldnames=["class", "service", "path", "package", "content_hash"]
        )
        writer.writeheader()
        writer.writerows(rows)

    # Print summary
    print(f"Wrote {OUT_PATH.relative_to(REPO_ROOT)} ({len(rows)} rows)")  # noqa
    print()
    print(f"  {'class':<28} {'copies':>6} {'unique':>6}")
    for cls in PROTECTED:
        cls_rows = [r for r in rows if r["class"] == cls]
        unique = len({r["content_hash"] for r in cls_rows})
        print(f"  {cls:<28} {len(cls_rows):>6} {unique:>6}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
