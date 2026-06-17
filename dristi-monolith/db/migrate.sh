#!/bin/sh
# =============================================================================
# dristi-monolith database migration script
#
# Runs Flyway against every sub-schema that was collected at image-build time.
# All SQL directories are mounted under /flyway/sql/<schema>/ so Flyway is
# invoked once per schema, each with its own schema-history table to keep
# the migration records isolated.
#
# Required environment variables (per schema):
#   DB_URL           – JDBC URL  (e.g. jdbc:postgresql://localhost:5432/dristi)
#   FLYWAY_USER      – DB user
#   FLYWAY_PASSWORD  – DB password
#
# Optional:
#   FLYWAY_EXTRA_ARGS – any additional Flyway flags appended to every invocation
# =============================================================================

set -e

FLYWAY_BIN="flyway"
COMMON_ARGS="-url=$DB_URL -user=$FLYWAY_USER -password=$FLYWAY_PASSWORD -baselineOnMigrate=true -outOfOrder=true $FLYWAY_EXTRA_ARGS"

run_schema() {
  SCHEMA="$1"
  LOCATION="/flyway/sql/$SCHEMA/main"
  TABLE="schema_version_${SCHEMA}"

  echo "──────────────────────────────────────────────"
  echo "Migrating schema: $SCHEMA"
  echo "  location : $LOCATION"
  echo "  table    : $TABLE"

  $FLYWAY_BIN $COMMON_ARGS \
    -table="$TABLE" \
    -locations="filesystem:$LOCATION" \
    migrate

  echo "✓  $SCHEMA migrated successfully"
}

# ── domain-case-lifecycle ──────────────────────────────────────────────────
run_schema abdiary
run_schema application
run_schema bailbond
run_schema casemanagement
run_schema cases
run_schema ctc
run_schema digitalizeddocuments
run_schema evidence
run_schema hearing
run_schema inportalsurvey
run_schema locksvc
run_schema notification
run_schema order
run_schema scheduler
run_schema task
run_schema taskmanagement
run_schema templateconfiguration

# ── domain-identity-access ─────────────────────────────────────────────────
run_schema advocate
run_schema advocateoffice

# ── domain-integration ────────────────────────────────────────────────────
run_schema epost
run_schema esign
run_schema icops
run_schema summons
run_schema treasury

# ── domain-payments ───────────────────────────────────────────────────────
run_schema calculator

echo "══════════════════════════════════════════════"
echo "All dristi-monolith schemas migrated successfully."
