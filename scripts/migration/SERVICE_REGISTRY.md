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
| `order` | 105 | pending | `case-lifecycle` | `order` | |
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
| `Notification` | 48 | pending | `case-lifecycle` | `notification` | |
| `digitalized-documents` | 105 | pending | `case-lifecycle` | `digitalizeddocuments` | |
| `ctc` | 125 | pending | `case-lifecycle` | `ctc` | |
| `template-configuration` | 29 | pending | `case-lifecycle` | `templateconfiguration` | |
| `ab-diary` | 91 | pending | `case-lifecycle` | `abdiary` | |
| `inportal-survey` | 43 | pending | `case-lifecycle` | `inportalsurvey` | |
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
| `epost-tracker` | 73 | pending | `integration` | `epost` | |
| `bank-details` | 15 | pending | `integration` | `bank` | smallest service — good warm-up |

## domain-payments

| Service | Files | Status | `--module` | `--subdomain` | Notes |
|---|---:|---|---|---|---|
| `payment-calculator-svc` | 107 | pending | `payments` | `calculator` | |

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
