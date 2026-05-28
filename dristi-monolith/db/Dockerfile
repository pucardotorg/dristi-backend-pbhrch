# =============================================================================
# dristi-monolith — DB migration image
#
# Build context must be the repository root (parent of dristi-monolith/).
# Example:
#   docker build -f dristi-monolith/db/Dockerfile \
#                -t dristi-monolith-db:latest .
#
# All sub-schema SQL files from every domain module are collected into a
# flat /flyway/sql/<schema>/ layout so the migrate.sh entrypoint can locate
# them by a simple, stable name regardless of where they live in the source
# tree.
# =============================================================================
FROM egovio/flyway:10.7.1

# ── domain-case-lifecycle ──────────────────────────────────────────────────
COPY dristi-monolith/domain-case-lifecycle/src/main/resources/abdiary/db/migration              /flyway/sql/abdiary/
COPY dristi-monolith/domain-case-lifecycle/src/main/resources/application/db/migration          /flyway/sql/application/
COPY dristi-monolith/domain-case-lifecycle/src/main/resources/bailbond/db/migration             /flyway/sql/bailbond/
COPY dristi-monolith/domain-case-lifecycle/src/main/resources/casemanagement/db/migration       /flyway/sql/casemanagement/
COPY dristi-monolith/domain-case-lifecycle/src/main/resources/cases/db/migration                /flyway/sql/cases/
COPY dristi-monolith/domain-case-lifecycle/src/main/resources/ctc/db/migration                  /flyway/sql/ctc/
COPY dristi-monolith/domain-case-lifecycle/src/main/resources/digitalizeddocuments/db/migration /flyway/sql/digitalizeddocuments/
COPY dristi-monolith/domain-case-lifecycle/src/main/resources/evidence/db/migration             /flyway/sql/evidence/
COPY dristi-monolith/domain-case-lifecycle/src/main/resources/hearing/db/migration              /flyway/sql/hearing/
COPY dristi-monolith/domain-case-lifecycle/src/main/resources/inportalsurvey/db/migration       /flyway/sql/inportalsurvey/
COPY dristi-monolith/domain-case-lifecycle/src/main/resources/locksvc/db/migration              /flyway/sql/locksvc/
COPY dristi-monolith/domain-case-lifecycle/src/main/resources/notification/db/migration         /flyway/sql/notification/
COPY dristi-monolith/domain-case-lifecycle/src/main/resources/order/db/migration                /flyway/sql/order/
COPY dristi-monolith/domain-case-lifecycle/src/main/resources/scheduler/db/migration            /flyway/sql/scheduler/
COPY dristi-monolith/domain-case-lifecycle/src/main/resources/task/db/migration                 /flyway/sql/task/
COPY dristi-monolith/domain-case-lifecycle/src/main/resources/taskmanagement/db/migration       /flyway/sql/taskmanagement/
COPY dristi-monolith/domain-case-lifecycle/src/main/resources/templateconfiguration/db/migration /flyway/sql/templateconfiguration/

# ── domain-identity-access ─────────────────────────────────────────────────
COPY dristi-monolith/domain-identity-access/src/main/resources/advocate/db/migration            /flyway/sql/advocate/
COPY dristi-monolith/domain-identity-access/src/main/resources/advocateoffice/db/migration      /flyway/sql/advocateoffice/

# ── domain-integration ────────────────────────────────────────────────────
COPY dristi-monolith/domain-integration/src/main/resources/epost/db/migration                   /flyway/sql/epost/
COPY dristi-monolith/domain-integration/src/main/resources/esign/db/migration                   /flyway/sql/esign/
COPY dristi-monolith/domain-integration/src/main/resources/icops/db/migration                   /flyway/sql/icops/
COPY dristi-monolith/domain-integration/src/main/resources/summons/db/migration                 /flyway/sql/summons/
COPY dristi-monolith/domain-integration/src/main/resources/treasury/db/migration                /flyway/sql/treasury/

# ── domain-payments ───────────────────────────────────────────────────────
COPY dristi-monolith/domain-payments/src/main/resources/calculator/db/migration                 /flyway/sql/calculator/

# ── migration entrypoint ───────────────────────────────────────────────────
COPY dristi-monolith/db/migrate.sh /usr/bin/migrate.sh
RUN chmod +x /usr/bin/migrate.sh

ENTRYPOINT ["/usr/bin/migrate.sh"]
