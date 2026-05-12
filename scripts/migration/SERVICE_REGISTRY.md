# Service Registry

Maps each DRISTI service to its target `(module, subdomain)` and tracks
migration progress. Refer to this when picking the next service to
migrate via [RUNBOOK.md](RUNBOOK.md).

**Status legend:**
- `done` — migrated, in `monolith/main` (or merged from a `monolith/<svc>` branch)
- `in-progress` — branch exists, PR open
- `pending` — not started
- `de-scoped` — explicitly excluded from migration

---

## Pipeline command (per service)

```bash
python3 scripts/migration/per_module/run_module_migration.py \
  --service <service> --module <module> --subdomain <subdomain>
```

Service / module / subdomain values come from this table.

---

## domain-case-lifecycle

Core case-lifecycle domain — the bulk of DRISTI.

| Service | Files | Status | `--module` | `--subdomain` | Notes |
|---|---:|---|---|---|---|
| `lock-svc` | 19 | **done** | `case-lifecycle` | `locksvc` | First test-drive |
| `case` | 330 | **done** | `case-lifecycle` | `cases` | Largest service; pipeline hardened against it |
| `hearing` | 196 | pending | `case-lifecycle` | `hearing` | |
| `order` | 105 | **done** | `case-lifecycle` | `order` | First service to drive Phase 35 contract-lift; Rule 29 workflow harmonization |
| `task` | 184 | pending | `case-lifecycle` | `task` | |
| `evidence` | 161 | pending | `case-lifecycle` | `evidence` | |
| `application` | 119 | pending | `case-lifecycle` | `application` | |
| `casemanagement` | 217 | pending | `case-lifecycle` | `casemanagement` | depends on `case` |
| `hearing-management` | 40 | pending | `case-lifecycle` | `hearingmanagement` | depends on `hearing` |
| `order-management` | 268 | pending | `case-lifecycle` | `ordermanagement` | depends on `order` |
| `task-management` | 153 | pending | `case-lifecycle` | `taskmanagement` | depends on `task` |
| `analytics` | 146 | pending | `case-lifecycle` | `analytics` | depends on `case` |
| `bail-bond` | 105 | pending | `case-lifecycle` | `bailbond` | depends on `case` |
| `transformer` | 122 | pending | `case-lifecycle` | `analytics` | merge into `analytics` subdomain |
| `Notification` | 48 | **done** | `case-lifecycle` | `notification` | First migration by an extended-team contributor; surfaced empty-package, YAML-stability, and pipeline-output staging gaps fixed in 5fb67371 |
| `digitalized-documents` | 105 | pending | `case-lifecycle` | `digitalizeddocuments` | |
| `ctc` | 125 | pending | `case-lifecycle` | `ctc` | |
| `template-configuration` | 29 | **done** | `case-lifecycle` | `templateconfiguration` | First migration on the parallel-migration kickoff recipe; leaf-service path validated (no `*Api`, `@ApplicationModule` for boundary, Rule 40 scan clean) |
| `ab-diary` | 91 | **done** | `case-lifecycle` | `abdiary` | Two-PR split (#66 structural+uplift, #68 dead-code sweep + Phase 4a revert). A-diary live; B-diary case-fetch deferred — wire `CaseApi` directly when resumed (Rule 32). FileStoreUtil lift to dristi-common deferred to its own Tier 3 PR |
| `inportal-survey` | 43 | **done** | `case-lifecycle` | `inportalsurvey` | Second leaf service after `template-configuration` (0 intra-DRISTI REST calls); 12 contract DTOs Phase-35-lifted to `dristi-common/contract/inportalsurvey/`; `@ApplicationModule` boundary marker, no `*Api` (no callers yet). Merge resolution (PR #64) surfaced that Pipeline 5 mechanically re-adds REST host keys to subdomain overlays after REST→direct cleanup commits → `SERVICE_DEAD_KEYS` denylist added in `run_consolidation.py` (483b71f8c) so ab-diary's `dristi.case.*` and payment-calculator's `egov.case.*` stay suppressed across future regens |
| `scheduler-svc` | 237 | pending | `case-lifecycle` | `scheduler` | depends on `hearing` |
| `openapi` | 260 | pending | `case-lifecycle` | `openapi` | |

## domain-identity-access

| Service | Files | Status | `--module` | `--subdomain` | Notes |
|---|---:|---|---|---|---|
| `advocate` | 87 | pending | `identity-access` | `advocate` | |
| `advocate-office-management` | 72 | pending | `identity-access` | `advocateoffice` | depends on `advocate` |

## domain-integration

External-system integrations.

| Service | Files | Status | `--module` | `--subdomain` | Notes |
|---|---:|---|---|---|---|
| `summons-svc` | 143 | pending | `integration` | `summons` | |
| `treasury-backend` | 115 | pending | `integration` | `treasury` | |
| `njdg-transformer` | 157 | pending | `integration` | `njdg` | |
| `icops_integration-kerala` | 89 | pending | `integration` | `icops` | |
| `e-sign-svc` + `esign-interceptor` | 68 | pending | `integration` | `esign` | merge both source services into one subdomain |
| `epost-tracker` | 73 | **done** | `integration` | `epost` | First domain-integration service to drive contract DTO lift; 20 hand-coded POJOs manually lifted to `dristi-common/contract/epost/` because source had no Swagger-generated DTOs (Phase 35 yielded zero). Rule 41 follow-up `5634d1cb9` qualified the 4 new epost stereotypes (Consumer/UserService/MdmsDataConfig/PdfServiceUtil); peer-side qualifier on order's `MdmsDataConfig` + abdiary's `PdfServiceUtil` deferred (each name has only 1 instance on main today, BeanNameCollisionTest green). REST→direct deferred: `SummonsHost` (summons-svc pending), `PdfServiceHost` (platform) |
| `bank-details` | 15 | **done** | `integration` | `bank` | First non-case-lifecycle service; surfaced /migrate-service Rule 28 violation, fixed in PR #55 |

## domain-payments

| Service | Files | Status | `--module` | `--subdomain` | Notes |
|---|---:|---|---|---|---|
| `payment-calculator-svc` | 107 | **done** | `payments` | `calculator` | First cross-Maven-module `*Api` consumer (calculator → `CaseApi`). Surfaced Rule 31a (`@NamedInterface("api")` for cross-Maven-module access) and Rule 41 (subdomain `Configuration`/`*Util`/`*Service` bean-name qualifier). Added `BeanNameCollisionTest` (PR #65) as static catch-net for Rule 41 |

## De-scoped (NOT migrated)

| Service | Reason |
|---|---|
| `ocr-service` | Phase 1 de-scope per the implementation plan |
| `sunbirdrc-credential-service` | Phase 1 de-scope |
| `artifacts` | Auxiliary build / not a service |
| `kerala-sms` | Out of monolith scope |
| `sbi-backend` | Out of monolith scope |

---

## Suggested order

**Sprint A (warm-up / smallest):**
1. `bank-details` (15 files, integration) — exercises the integration
   module path with minimal moving parts.
2. `template-configuration` (29 files, case-lifecycle).
3. `inportal-survey` (43 files, case-lifecycle).

**Sprint B (core case lifecycle):** any of `hearing`, `order`, `task`,
`evidence`, `application` — independent of each other; pick by file
count if you want the smallest first.

**Sprint C (depends on B):** `casemanagement`, `hearing-management`,
`order-management`, `task-management`, `analytics`, `bail-bond`,
`scheduler-svc`. Migrate the dependency first (e.g. `case` before
`casemanagement`).

**Sprint D (identity + integration):** `advocate` →
`advocate-office-management`; then the integration services in any
order.

**Sprint E:** `payment-calculator-svc`, `transformer`, `Notification`,
`openapi`, leftover case-lifecycle services.

---

## Updating this registry

Each successful migration PR should bump the corresponding row to
`done` (or `in-progress` while the PR is open). Treat this as the
authoritative ledger.

If a service splits or merges with another (e.g. `transformer` →
`case-lifecycle/analytics`), update the **Notes** column and record
the merge in the PR description.
