#!/usr/bin/env bash
# Single-command per-service migration driver.
# Chains the per-module pipeline, Pipeline 5 (config consolidation), and a
# build smoke-test, then prints a checklist of manual follow-ups.
#
# Usage:
#   scripts/migration/run_pipeline.sh <service> <module> <subdomain>
#
# Example:
#   scripts/migration/run_pipeline.sh hearing case-lifecycle hearing
set -euo pipefail

if [ $# -ne 3 ]; then
    echo "Usage: $(basename "$0") <service> <module> <subdomain>"
    echo "  e.g. $(basename "$0") hearing case-lifecycle hearing"
    echo
    echo "See scripts/migration/SERVICE_REGISTRY.md for the right (module, subdomain) per service."
    exit 64
fi

SERVICE="$1"
MODULE="$2"
SUBDOMAIN="$3"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
cd "$REPO_ROOT"

echo "=== Per-module pipeline: $SERVICE → domain-$MODULE/$SUBDOMAIN ==="
python3 "$SCRIPT_DIR/per_module/run_module_migration.py" \
    --service "$SERVICE" --module "$MODULE" --subdomain "$SUBDOMAIN"

echo
echo "=== Pipeline 5 (config consolidation) ==="
# Auto-discover every subdomain that already has migrated code so we
# don't drop earlier services' per-subdomain YMLs by passing too few
# --service flags.
mapfile -t MIGRATED_SERVICES < <(
    find "$REPO_ROOT/scripts/migration/per_module/output" \
        -maxdepth 1 -name '*_manifest.json' -printf '%f\n' 2>/dev/null \
        | sed 's/_manifest\.json$//' \
        | sort -u
)
if [ "${#MIGRATED_SERVICES[@]}" -eq 0 ]; then
    MIGRATED_SERVICES=("$SERVICE")
fi
SVC_FLAGS=()
for s in "${MIGRATED_SERVICES[@]}"; do
    SVC_FLAGS+=(--service "$s")
done
echo "  consolidating: ${MIGRATED_SERVICES[*]}"
python3 "$SCRIPT_DIR/config_consolidation/run_consolidation.py" "${SVC_FLAGS[@]}"

echo
echo "=== Smoke build (mvn -pl dristi-app -am package -DskipTests) ==="
JAVA_HOME_DEFAULT=$HOME/.jdks/corretto-17.0.18
if [ -d "$JAVA_HOME_DEFAULT" ] && ! command -v javac >/dev/null 2>&1; then
    export JAVA_HOME="$JAVA_HOME_DEFAULT"
    export PATH="$JAVA_HOME/bin:$PATH"
fi
( cd "$REPO_ROOT/dristi-monolith" && mvn -B -pl dristi-app -am package -DskipTests )

echo
echo "=== Done. Next steps ==="
echo "  1. Add '$SUBDOMAIN' to spring.profiles.active in"
echo "     dristi-monolith/dristi-app/src/main/resources/application.yml"
echo "  2. Convert intra-DRISTI REST calls listed in:"
echo "     scripts/migration/per_module/output/${SERVICE}_rest_calls.txt"
echo "  3. Review follow-ups (if any):"
echo "     scripts/migration/per_module/output/${SERVICE}_followups.txt"
echo "  4. Run tests:"
echo "     cd dristi-monolith && mvn -B -pl domain-$MODULE -am test \\"
echo "       -Dsurefire.failIfNoSpecifiedTests=false"
echo "  5. Open the PR per RUNBOOK.md §8."
