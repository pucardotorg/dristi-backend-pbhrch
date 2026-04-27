#!/usr/bin/env bash
# Pipeline 1 — Monolith Scaffold Generator (run all 4 phases + validate).
#
# Idempotent: each phase rewrites files in place, so re-runs are safe.
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/../../.." && pwd)"

cd "$REPO_ROOT"

echo "=== Phase 1: Generate parent POM ==="
python3 "$SCRIPT_DIR/01_generate_parent_pom.py"

echo
echo "=== Phase 2: Generate module skeletons ==="
python3 "$SCRIPT_DIR/02_generate_module_skeletons.py"

echo
echo "=== Phase 3: Generate bootstrap app ==="
python3 "$SCRIPT_DIR/03_generate_bootstrap_app.py"

echo
echo "=== Phase 4: mvn validate ==="
cd dristi-monolith
mvn -B -q validate
echo "PASS — dristi-monolith parent + 6 modules validate cleanly"
