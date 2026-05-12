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
| `bail-bond` | 105 | **done** | `case-lifecycle` | `bailbond` | PR #70 merged to `monolith/main` as `3fcd07652` on 2026-05-12. Lifted into `domain-case-lifecycle/bailbond`; 28 contract DTOs Phase-35-lifted to `dristi-common/contract/bailbond/`; `CaseUtil` swap from `cases.internal.service.CaseService` to `cases.CaseApi.search()` (Rule 3 + 32); Rule 41 follow-ups for `MainConfiguration` + 12 other stereotypes; bailbond `@ApplicationModule` boundary marker |
| `transformer` | 122 | pending | `case-lifecycle` | `analytics` | merge into `analytics` subdomain |
| `Notification` | 48 | **done** | `case-lifecycle` | `notification` | First migration by an extended-team contributor; surfaced empty-package, YAML-stability, and pipeline-output staging gaps fixed in 5fb67371 |
| `digitalized-documents` | 105 | **done** | `case-lifecycle` | `digitalizeddocuments` | PR #76 merged to `monolith/main` as `289eafb71` on 2026-05-12. Lifted into `domain-case-lifecycle/digitalizeddocuments`; 25 contract DTOs Phase-35-lifted to `dristi-common/contract/digitalizeddocuments/` (`CaseSearchRequest`/`CaseCriteria` dropped post-lift as `cases` canonical shadows); `CaseUtil` swap to `cases.CaseApi.search()` (Rule 3 + 32). Tier 3 lifts into `dristi-common`: `ServiceRequestRepository.fetchResultGetForResource(StringBuilder)`, `UrlShortenerUtil.expireTheUrl` generalized to `(url, referenceId)` per Rule 18, `FileStoreUtil` (`module` is now a parameter, was hardcoded `signed`), and new `FileValidationUtil`. Merge with bail-bond (`aebdb914c`) surfaced Rule 41 bean-name collisions on `ESignUtil`/`CipherUtil`/`XmlRequestGenerator` — bail-bond merged first, so digitalized-documents side absorbed the qualifier rename to `digitalizeddocuments*`. `@ApplicationModule` boundary marker; no `*Api` (no cross-subdomain consumers yet) |
| `ctc` | 125 | pending | `case-lifecycle` | `ctc` | |
| `template-configuration` | 29 | **done** | `case-lifecycle` | `templateconfiguration` | First migration on the parallel-migration kickoff recipe; leaf-service path validated (no `*Api`, `@ApplicationModule` for boundary, Rule 40 scan clean) |
| `ab-diary` | 91 | **done** | `case-lifecycle` | `abdiary` | Two-PR split (#66 structural+uplift, #68 dead-code sweep + Phase 4a revert). A-diary live; B-diary case-fetch deferred — wire `CaseApi` directly when resumed (Rule 32). FileStoreUtil lift to dristi-common deferred to its own Tier 3 PR |
| `inportal-survey` | 43 | **done** | `case-lifecycle` | `inportalsurvey` | Second leaf service after `template-configuration` (0 intra-DRISTI REST calls); 12 contract DTOs Phase-35-lifted to `dristi-common/contract/inportalsurvey/`; `@ApplicationModule` boundary marker, no `*Api` (no callers yet). Merge resolution (PR #64) surfaced that Pipeline 5 mechanically re-adds REST host keys to subdomain overlays after REST→direct cleanup commits → `SERVICE_DEAD_KEYS` denylist added in `run_consolidation.py` (483b71f8c) so ab-diary's `dristi.case.*` and payment-calculator's `egov.case.*` stay suppressed across future regens |
| `scheduler-svc` | 237 | pending | `case-lifecycle` | `scheduler` | depends on `hearing` |
| `openapi` | 260 | pending | `case-lifecycle` | `openapi` | |

## domain-identity-access

| Service | Files | Status | `--module` | `--subdomain` | Notes |
|---|---:|---|---|---|---|
| `advocate` | 87 | **done** | `identity-access` | `advocate` | PR #67 merged to `monolith/main` as `40e5afcbd` on 2026-05-12. First `identity-access` subdomain — created `domain-identity-access` Maven module with `advocate` as its first occupant. 24 contract DTOs Phase-35-lifted to `dristi-common/contract/advocate/`. Exposed `AdvocateApi` (+ `@NamedInterface("api")` + `@ApplicationModule`); first non-case-lifecycle subdomain to ship a cross-Maven `*Api` (Rule 31 + 31a). 5 REST call sites in `cases` converted to direct `AdvocateApi.search()` per Rule 32 (`CaseRegistrationEnrichment`, `EncryptionDecryptionUtil`, `EnrichCaseWhenESign`, `CaseRegistrationValidator`, `CaseService`); REST utils deleted per Rule 38 (`cases/AdvocateUtil`, `order/AdvocateUtil`, `AdvocateUtilTest`); `domain-case-lifecycle/pom.xml` dependency added on `domain-identity-access`. Post-merge chore (`e1d23287d`) swept dead `advocateHost`/`advocatePath` `@Value` fields + YAML blocks from `cases`/`order` Configuration; moved `egov.hrms.host` localhost override into `application-local.yml`. `UserUtil.parseResponse(Map<S,O>)` and `IndividualUtil.individualCall(3-param UUID variant)` kept service-local |
| `advocate-office-management` | 72 | **done** | `identity-access` | `advocateoffice` | PR #81 merged to `monolith/main` as `635960916` on 2026-05-12. Lifted into `domain-identity-access/advocateoffice` (second occupant of `domain-identity-access` after `advocate`). C1 `fde4c91a3` = structural lift. C2 `876090e33` = contract uplift (Phase 35) + REST→direct. `a2406cfd0` consolidated to canonical `commonIndividualUtil` — deleted local `IndividualUtil` copies in both `advocate` and `advocateoffice`; `IndividualService` (advocate) switched to `@Qualifier("commonIndividualUtil")` with inline `userUuid` extraction; `AdvocateOfficeEnrichment` switched to `commonIndividualUtil` with local `Configuration` for URI building. `8e1e2c2c3` uplifted `AdvocateUtil` REST → `AdvocateApi` direct (Rule 32 + Rule 38): deleted `AdvocateUtil` + `AdvocateUtilTest`, replaced `AdvocateOfficeValidator`/`AdvocateOfficeEnrichment` callers with typed `AdvocateApi` returning `Advocate`/`AdvocateClerk` DTOs; added `AdvocateApi.searchClerksById(RequestInfo, tenantId, clerkId)` filling a Rule 39 gap (implemented in `AdvocateApiImpl` delegating to `AdvocateClerkService`); `tenantId` now forwarded instead of `null`; dead `dristi.advocate.*` keys swept from `application-advocateoffice.yml`. `5f66fa119` reverted port numbers (local-dev hygiene). **Follow-up**: `CaseUtil → CaseApi` conversion deferred — would require `domain-identity-access → domain-case-lifecycle`, which cycles back through `case-lifecycle → identity-access` for `AdvocateApi`; Tier 3 design decision. |

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
