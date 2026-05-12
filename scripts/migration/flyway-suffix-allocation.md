# Flyway Suffix & Normalization Allocation

Pre-computed Phase 9 actions for the 23 pending services that carry
Flyway migrations. Generated 2026-05-07 from `monolith/main` state.

**Purpose.** Read by Claude during `/migrate-service` Phase 9 / Gate 7
fixes so collision suffixes and timestamp normalization are applied
deterministically. No ad-hoc decisions across parallel sessions.

**Re-validate** before relying on this table — see [Verification](#verification)
at the bottom.

---

## Already in `monolith/main` (reference — do not edit)

| Subdomain | Migration | Note |
|---|---|---|
| `cases` | `V20240424110535__case__ddl.sql` | First claimant of this timestamp |
| `order` | `V20240424110535_2__order__ddl.sql` | `_2` applied to break collision with `cases` |
| `locksvc` | `V20250117154400__lock__ddl.sql` | No collision |
| `notification` | `V20250210110700__notification__ddl.sql` | No collision |
| _bank-details_ | _(no DDL)_ | Service has no Flyway migrations |

---

## Quick lookup — services requiring Phase 9 action

Five pending services need a non-trivial Phase 9 step. Everything else
copies through unchanged.

| Service | Reason | Source filename | Target filename in monolith | Tier |
|---|---|---|---|---|
| `task` | Collision with `cases` (V20240424110535) + `order` (`_2`) | `V20240424110535__task__ddl.sql` | `V20240424110535_3__task__ddl.sql` | 1 — Claude applies |
| `icops_integration-kerala` | Collision with `epost-tracker` (V20240723134500) | `V20240723134500__icops-tracker_ddl.sql` | `V20240723134500_2__icops-tracker_ddl.sql` | 1 — Claude applies *only if* `epost-tracker` is already `done`; otherwise see [Claim order](#claim-order) |
| `bail-bond` | Malformed timestamp (8 chars), pad with zeros | `V20251120__sureity__email__ddl.sql` | `V20251120000000__sureity__email__ddl.sql` | 1 — Claude pads deterministically |
| `casemanagement` | Malformed timestamp (15 chars), suspect typo | `V202400714144420__reference_filestore__ddl.sql` | `V20240714144420__reference_filestore__ddl.sql` (proposed: drop extra `0` after `2024`) | **4** — dev confirms via `git log` of the SQL file |
| `openapi` | Malformed timestamp (13 chars), year ambiguous | `V2020709230000__landing_page_notices__ddl.sql` | `V20200709230000__...` *or* `V20240709230000__...` | **4** — dev confirms via `git log` of the SQL file to disambiguate year |

### Why normalize at all?

Beyond cosmetics — Flyway sorts numerically, and a 14-digit version
sorts *after* an 8-digit version of the same prefix. Without normalizing
`bail-bond`'s `V20251120`, it would attempt to apply *before* `cases`'s
`V20240424110535` (since 20,251,120 < 20,240,424,110,535). On a fresh
local DB that's a real risk if `bail-bond`'s schema references `cases`.
Padding restores the intended chronological order.

### Claim order

For the `epost-tracker` / `icops_integration-kerala` pair, both pending
on the same timestamp:

- **First to migrate** keeps the original filename.
- **Second to migrate** applies `_2`.

The lane plan assigns `epost-tracker` first (smaller, earlier in the
queue), so the table above reflects that order. If the order flips for
any reason, swap the rename: whichever lands in `monolith/main` first
keeps the original.

---

## Full pending-service table

| Service | Subdomain | Source migration | Phase 9 action |
|---|---|---|---|
| `ab-diary` | `abdiary` | `V20250115184300__casediary__ddl.sql` | copy as-is |
| `advocate` | `advocate` | `V20240313110535__ADVClerk_ddl.sql` | copy as-is |
| `advocate-office-management` | `advocateoffice` | `V20260121173000__advocate-office-management__ddl.sql` | copy as-is |
| `application` | `application` | `V20240514192045__application__ddl.sql` | copy as-is |
| **`bail-bond`** | `bailbond` | `V20251120__sureity__email__ddl.sql` | **normalize → `V20251120000000__sureity__email__ddl.sql` (Tier 1)** |
| **`casemanagement`** | `casemanagement` | `V202400714144420__reference_filestore__ddl.sql` | **normalize via Tier 4 — dev confirms target** |
| `ctc` | `ctc` | `V20260227111500__ctc_applications__ddl.sql` | copy as-is |
| `digitalized-documents` | `digitalizeddocuments` | `V20251125202000__digitalzeddocuments__ddl.sql` | copy as-is |
| `e-sign-svc` (+ `esign-interceptor`) | `esign` | `V20241017153200__esign__ddl.sql` | copy as-is (esign-interceptor has no DDL) |
| `epost-tracker` | `epost` | `V20240723134500__epost_tracker_ddl.sql` | copy as-is — claim timestamp first |
| `evidence` | `evidence` | `V20240403110535__evidence__ddl.sql` | copy as-is |
| `hearing` | `hearing` | `V20240514110535__hearing__ddl.sql` | copy as-is |
| **`icops_integration-kerala`** | `icops` | `V20240723134500__icops-tracker_ddl.sql` | **suffix `_2` (Tier 1) — only if `epost-tracker` is already `done`** |
| `inportal-survey` | `inportalsurvey` | `V20251015115000__inportal_survey__ddl.sql` | copy as-is |
| **`njdg-transformer`** | `njdg` | `V20251008141800__create__ddl.sql` + non-standard `db/migration-main/<dir>/*.sql` | **see [Special: njdg-transformer](#special-njdg-transformer)** |
| **`openapi`** | `openapi` | `V2020709230000__landing_page_notices__ddl.sql` | **normalize via Tier 4 — dev confirms target year** |
| `payment-calculator-svc` | `calculator` | `V20240611165500__ph_postal_hub_ddl.sql` | copy as-is |
| `scheduler-svc` | `scheduler` | `V20240415195100__hb_hearing_booking_ddl.sql` | copy as-is |
| `summons-svc` | `summons` | `V20240529100725__summons_ddl.sql` | copy as-is |
| **`task`** | `task` | `V20240424110535__task__ddl.sql` | **suffix `_3` (Tier 1)** |
| `task-management` | `taskmanagement` | `V20251024110535__task-management__ddl.sql` | copy as-is |
| `template-configuration` | `templateconfiguration` | `V20250128110535__template__ddl.sql` | copy as-is |
| `treasury-backend` | `treasury` | `V20240708134500__treasury_backend_ddl.sql` | copy as-is |

### Pending services with no migrations

Phase 9 has nothing to copy; Gate 6 will report `0 SQL files`. No
`spring.flyway.locations` entry needed.

- `analytics`, `transformer`, `hearing-management`, `order-management`
- `esign-interceptor` (merges into `e-sign-svc`'s `esign` subdomain — that side carries the DDL)

---

## Special: `njdg-transformer`

Source layout does not match Phase 9's `db/migration/main/V*.sql` walker:

```
integration-services/njdg-transformer/src/main/resources/
├── db/migration-ddl/
│   └── V20251008141800__create__ddl.sql                 ← Flyway-style, but in a non-standard dir
└── db/migration-main/
    └── 20151212145100/                                  ← timestamped *directory*, files lack V<ts>__ prefix
        ├── court_info.sql
        ├── ia_filings.sql
        ├── ... (29 files)
```

**When `njdg-transformer` is claimed, before reaching Phase 9 the dev
must resolve this — Tier 4.** Three options to lay out:

1. **Pre-flight reorganization.** Restructure source files into the
   standard `db/migration/main/V<ts>__<name>.sql` layout in a separate
   cleanup PR, merge that first, then migrate normally.
2. **Phase 9 enhancement.** Extend the pipeline walker to handle the
   dual layout. Codify as a new rule. Tier 2 pipeline change.
3. **Skip Flyway for njdg-transformer.** If these scripts already run
   via a different mechanism in production and are not expected to be
   re-applied by the monolith, Phase 9 becomes a no-op and the
   migration proceeds without DB scope.

Don't proceed to Phase 9 on `njdg-transformer` until this is decided.

---

## Cutover-time concern (out of scope for per-service migration)

Local development uses fresh docker Postgres each boot via
`scripts/local-test/`, so the normalized/suffixed names apply cleanly.

When the monolith promotes to a **shared** test/staging env that reuses
the legacy per-service `flyway_schema_history` tables, the renamed
versions won't match recorded versions and Flyway will refuse to boot.
The cutover team will need to choose:

- **A.** Drop legacy `flyway_schema_history` rows below the monolith
  cutoff; let the monolith re-apply against the existing schemas.
- **B.** Configure Flyway baseline at monolith boot to ignore versions
  below a cutoff.
- **C.** Promote into a fresh schema; legacy data is out of scope.

This is **not** a Day-1 blocker. Capture in `FOLLOWUP_FLYWAY_CUTOVER.md`
when the promotion plan firms up.

---

## Verification

Re-run these before relying on this table:

```bash
# What's currently in monolith/main
find dristi-monolith -path "*/db/migration/*/V*.sql" -not -path "*/target/*" -type f | sort

# What pending services bring in
find dristi-services integration-services -path "*/db/migration/main/V*.sql" -not -path "*/target/*" -type f | sort
```

If output diverges from the tables above (a service moved to `done`,
or a new collision appeared), regenerate this file before next claim.
