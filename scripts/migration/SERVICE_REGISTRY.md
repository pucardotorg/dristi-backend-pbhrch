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
| `hearing` | 196 | **done** | `case-lifecycle` | `hearing` | PR #57 merged to `monolith/main` as `690b8a925` on 2026-05-14. Lifted into `domain-case-lifecycle/hearing`; 47 contract DTOs Phase-35-lifted; exposed `HearingApi` (Rule 31 + 31a); cases-side swapped to `HearingApi.search()` per Rule 32 (927ebb6c5). Post-merge PR #92 (`7222096fa`, 2026-05-14) shipped Rule 24a shadow-import sweep (24 sites), Rule 32 cutovers for `ctc` + `order` callers + symmetric hearing→cases (`CaseUtil → CaseApi` missed by PR #57), `SERVICE_DEAD_KEYS` extension, whitespace cleanup. Rule 40 reviewed → no `RequestInfo` mutations. Follow-up: bailbond `@Qualifier("hearingCaseUtil")` leak deferred. |
| `order` | 105 | **done** | `case-lifecycle` | `order` | First service to drive Phase 35 contract-lift; Rule 29 workflow harmonization |
| `task` | 184 | **done** | `case-lifecycle` | `task` | PR #89 merged to `monolith/main` as `73cbcf3a0` on 2026-05-14. **C1** `3c664e077` = structural lift, 10 protected dups deduped, Rule 41 peer-side qualifiers. **C2** `034dfe386` = Phase 35 contract uplift + Rule 32 REST→direct conversions. **C3** `a89087936` = first cycle-break of the cases↔task↔order Spring bean cycle: introducing `TaskApi` made the dependency graph cyclic; reverted cases→task to REST (kept task→cases direct via `CaseApi`). Established the **incoming-edge sever** pattern later reused by task-management's `1616b5ad5`. **C4** `e6dd837fe` = post-merge boot fix qualifying hearing+task util bean names (DemandUtil/JsonUtil/OrderUtil/PendingTaskUtil) per Rule 41. |
| `evidence` | 161 | **done** | `case-lifecycle` | `evidence` | PR #97 merged to `monolith/main` as `37e417943` on 2026-05-19. **C1** `df05e0a63` = structural lift of 132 main + 29 test files, 15 protected dups deduped (incl. `WorkflowObject`/`ProcessInstanceObject`); EvidenceValidator import swap `commons-lang3.ObjectUtils → spring.util.ObjectUtils` (parent pom pins commons-lang3 3.4 without `isEmpty`). **C2** `ea4cbf89e` = 77 contract DTOs Phase-35-lifted to `dristi-common/contract/evidence/`; `EvidenceApi.searchEvidence` exposed (read-only, AdvocateApi-style with explicit `RequestInfo` per Rule 33) + `@ApplicationModule(displayName="Evidence")` + `@NamedInterface("api")`. Rule 32 cutovers: cases/`EvidenceValidator` → `EvidenceApi` (3 callsites, `Pagination` literals 100→100.0 for Double field); evidence/`HearingUtil` → `AdvocateApi.getAdvocateIndividualIds`. Rule 38 sweep: deleted evidence/`AdvocateUtil` + cases-local orphan DTOs (`EvidenceSearchCriteria`/`Request`/`Response`). cases/`EvidenceUtil` trimmed to `createEvidence`-only REST shim (Rule 35 cross-subdomain write deferred — every existing `*Api` is read-only). Post-merge chore `5f37fc2ca` extended `SERVICE_DEAD_KEYS["case"]` with `egov.evidence.search.path`. Deferred per Rule 39 / Rule 35: `createEvidence` (cases→evidence write); evidence-side `CaseUtil`/`OrderUtil`/`ApplicationUtil`/`HearingUtil.fetchHearing*` (target gaps or DTO-shape mapping). |
| `application` | 119 | **done** | `case-lifecycle` | `application` | PR #91 merged to `monolith/main` as `cd48feb38` on 2026-05-18. **C1** `4f6512c7f` = structural lift of 91 main + 19 test files, 14 protected dups deduped; `DateUtil.getCurrentYear()` lifted to `dristi-common` canonical; `Document` canonical gained `documentOrder` field; Rule 41 peer-side qualifiers on cases's `BillingUtil`+`EvidenceUtil`. **C2** `9b9a3bd35` = 45 contract DTOs Phase-35-lifted to `dristi-common/contract/application/`; `@ApplicationModule` boundary marker; Rule 40 defensive-copy fixes in `ApplicationService`+`PaymentUpdateService`. **C3** `5cffe34c1` = Rule 32 `CaseUtil` REST→`CaseApi.exists()`/`search()` direct; Rule 37 swept dead `caseHost`/`caseExistsPath`/`caseSearchPath` from `Configuration`. No `ApplicationApi` yet (no in-tree callers). Deferred: `OrderUtil` REST→direct (`OrderApi` lacks `exists()`, Rule 39); `BillingUtil`/`DemandUtil` (Rule 17 platform); `EvidenceUtil` (pre-evidence merge). Post-merge `DemandUtil` bean-name collision with `treasury` resolved in `c9de3240c`. |
| `casemanagement` | 217 | pending | `case-lifecycle` | `casemanagement` | depends on `case` |
| `hearing-management` | 40 | pending | `case-lifecycle` | `hearingmanagement` | depends on `hearing` |
| `order-management` | 268 | pending | `case-lifecycle` | `ordermanagement` | depends on `order` |
| `task-management` | 153 | **done** | `case-lifecycle` | `taskmanagement` | PR #95 merged to `monolith/main` as `27201ddc6` on 2026-05-18. 134 main + 13 test files; 10 protected dups deduped; `DateUtil.getCurrentTimeInMilis()`+`getEpochFromDateString()` lifted to `dristi-common` canonical (5 unused dropped per Rule 18). Phase 35 **reverted** — 21 DTOs returned to `taskmanagement/internal/web/models/` with `@NamedInterface("contract")` per cases/lock-svc precedent (lifted DTOs cross-referenced still-internal subpackage types). `TaskmanagementApi` exposed: `search()`+`update()`. Rule 32 cutovers: hearing→taskmanagement (`TaskmanagementApi` in `OrderUtil`) + taskmanagement→cases (`CaseApi` in `CaseUtil`). `1616b5ad5` cycle-break: the two simultaneous direct calls closed a 3-node loop `cases→hearing→taskmanagement→cases`; per the task-module precedent (`a89087936`), severed the **incoming** edge — restored hearing's `TaskManagementUtil` REST helper + 14 DTOs + Configuration `dristi.task-management.*` fields + `application-hearing.yml` block. `TaskmanagementApi` interface kept exposed for future non-cyclic callers. Rule 40 defensive-copy fixes across `Consumer`/`PaymentUpdateService`/`TaskCreationService`. Post-merge `ETreasuryUtil` bean-name collision with `treasury` resolved in `c9de3240c`. |
| `analytics` | 146 | **done** | `case-lifecycle` | `analytics` | PR #106 merged to `monolith/main` as `3910d9461` on 2026-05-21. C1 `e2628686d` = structural lift (115 main + 31 test files into `domain-case-lifecycle/analytics`; 6 cross-service utils consolidated into `dristi-common` — MdmsUtil/IndividualUtil/ResponseInfoFactory/Producer/KafkaProducerService/ServiceRequestRepository; `analytics` profile wired into `spring.profiles.active`; `spring-boot-starter-cache` lifted into `domain-case-lifecycle/pom.xml`; Rule 26 Pattern B applied at 7 raw-JSON call sites). C2 `73eef3e08` = 17 contract DTOs Phase-35-lifted to `dristi-common/contract/analytics/` with `@ApplicationModule(displayName="Analytics")` boundary + `@NamedInterface("contract-analytics")`; 6 REST → direct (`AdvocateApi`/`CaseApi`/`EvidenceApi`/`HearingApi`/`OrderApi`/`TaskManagementApi`). C3 `dd48320ca` = `EventConsumerConfig` `@PropertySource` switched to `classpath:application-analytics.yml`. **createPendingTask write stays REST** (Rule 35 deferral — exposing `AnalyticsApi.createPendingTask` would close a Spring bean cycle with hearing/task/taskmanagement). `CtcApplicationUtil` REST kept (ctc not yet migrated); `PendingTaskUtil` REST kept (direct ES, no peer). **Follow-up PR #108** merged to `monolith/main` as `19631fff1` on 2026-05-21 (`0395b1d2d`) — extends `SERVICE_DEAD_KEYS["analytics"]` for 11 keys + scrubs `application-analytics.yml`; pre-Rule-42 manual fix. From now on Rule 42's Phase 95+96 (PR #107 `c3f8fd02a`) auto-handles equivalent cases for every future migration. 8 `WorkflowUtil` methods missing from canonical = Tier 3 widening. |
| `bail-bond` | 105 | **done** | `case-lifecycle` | `bailbond` | PR #70 merged to `monolith/main` as `3fcd07652` on 2026-05-12. Lifted into `domain-case-lifecycle/bailbond`; 28 contract DTOs Phase-35-lifted to `dristi-common/contract/bailbond/`; `CaseUtil` swap from `cases.internal.service.CaseService` to `cases.CaseApi.search()` (Rule 3 + 32); Rule 41 follow-ups for `MainConfiguration` + 12 other stereotypes; bailbond `@ApplicationModule` boundary marker |
| `transformer` | 122 | in-progress | `case-lifecycle` | `analytics` | PR open on `monolith/transformer` (head `9b7feb14d`, 2026-05-18). Merge into `analytics` subdomain. |
| `Notification` | 48 | **done** | `case-lifecycle` | `notification` | First migration by an extended-team contributor; surfaced empty-package, YAML-stability, and pipeline-output staging gaps fixed in 5fb67371 |
| `digitalized-documents` | 105 | **done** | `case-lifecycle` | `digitalizeddocuments` | PR #76 merged to `monolith/main` as `289eafb71` on 2026-05-12. Lifted into `domain-case-lifecycle/digitalizeddocuments`; 25 contract DTOs Phase-35-lifted to `dristi-common/contract/digitalizeddocuments/` (`CaseSearchRequest`/`CaseCriteria` dropped post-lift as `cases` canonical shadows); `CaseUtil` swap to `cases.CaseApi.search()` (Rule 3 + 32). Tier 3 lifts into `dristi-common`: `ServiceRequestRepository.fetchResultGetForResource(StringBuilder)`, `UrlShortenerUtil.expireTheUrl` generalized to `(url, referenceId)` per Rule 18, `FileStoreUtil` (`module` is now a parameter, was hardcoded `signed`), and new `FileValidationUtil`. Merge with bail-bond (`aebdb914c`) surfaced Rule 41 bean-name collisions on `ESignUtil`/`CipherUtil`/`XmlRequestGenerator` — bail-bond merged first, so digitalized-documents side absorbed the qualifier rename to `digitalizeddocuments*`. `@ApplicationModule` boundary marker; no `*Api` (no cross-subdomain consumers yet) |
| `ctc` | 125 | **done** | `case-lifecycle` | `ctc` | PR #86 merged to `monolith/main` as `948e19adb` on 2026-05-13. **C1** `447c341d7` = structural lift to `domain-case-lifecycle/ctc`. **C2** `3f4615c58` = contract uplift + REST→direct calls. **C3** `1993eaa04` = Rule 38 + Rule 29C post-merge cleanups. **C4** `59d657c6e` = `HAND-CURATED` marker added to `CtcFileStoreHelper` (Rule 12). Follow-up PR #87 (`00d30ad89`, 2026-05-13) shipped Rule 40 shadow-import sweep. Subsequent `bcf520653` paired ctc + order REST→HearingApi direct calls during the hearing-PR cutover. |
| `template-configuration` | 29 | **done** | `case-lifecycle` | `templateconfiguration` | First migration on the parallel-migration kickoff recipe; leaf-service path validated (no `*Api`, `@ApplicationModule` for boundary, Rule 40 scan clean) |
| `ab-diary` | 91 | **done** | `case-lifecycle` | `abdiary` | Two-PR split (#66 structural+uplift, #68 dead-code sweep + Phase 4a revert). A-diary live; B-diary case-fetch deferred — wire `CaseApi` directly when resumed (Rule 32). FileStoreUtil lift to dristi-common deferred to its own Tier 3 PR |
| `inportal-survey` | 43 | **done** | `case-lifecycle` | `inportalsurvey` | Second leaf service after `template-configuration` (0 intra-DRISTI REST calls); 12 contract DTOs Phase-35-lifted to `dristi-common/contract/inportalsurvey/`; `@ApplicationModule` boundary marker, no `*Api` (no callers yet). Merge resolution (PR #64) surfaced that Pipeline 5 mechanically re-adds REST host keys to subdomain overlays after REST→direct cleanup commits → `SERVICE_DEAD_KEYS` denylist added in `run_consolidation.py` (483b71f8c) so ab-diary's `dristi.case.*` and payment-calculator's `egov.case.*` stay suppressed across future regens |
| `scheduler-svc` | 237 | in-progress | `case-lifecycle` | `scheduler` | PR open on `monolith/scheduler-svc` (head `9a18dc2f0`, 2026-05-19). Depends on `hearing`. |
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
| `treasury-backend` | 115 | **done** | `integration` | `treasury` | PR #88 merged to `monolith/main` as `2a10ffb95` on 2026-05-18. Lifted 90 main + 13 test files into `domain-integration/treasury`; 7 protected dups deduped (`MdmsUtil`/`IdgenUtil`/`IndividualUtil`/`ResponseInfoFactory`/`Producer`/`KafkaProducerService`/`ServiceRequestRepository`, plus `Individual` model). Adopted canonical `IdgenUtil.getIdList` (6-arg) — dropped service-local copy. Rule 41 qualifiers `treasuryIndividualService` + `treasuryCaseUtil`. `SERVICE_DEAD_KEYS["case"]` extended with 4 etreasury keys (cases now reads `TreasuryApi` direct, no `egov.etreasury.*` consumer remains). Post-merge bean-name collisions `ETreasuryUtil`/`DemandUtil` with `taskmanagement`/`application` resolved in `c9de3240c` via Rule 41 subdomain-prefixed qualifiers. |
| `njdg-transformer` | 157 | in-progress | `integration` | `njdg` | PR open on `monolith/njdg-transformer` (head `e6e7d61b1`, 2026-05-18). |
| `icops_integration-kerala` | 89 | pending | `integration` | `icops` | |
| `e-sign-svc` + `esign-interceptor` | 68 | **done** | `integration` | `esign` | PR #84 merged to `monolith/main` as `13c12d5f0` on 2026-05-12. Two-service merge — hand-port pattern used. **PR 1** (`monolith/esign`, 3 commits): C1 `66c930c1c` = `e-sign-svc` structural lift to `domain-integration/esign` (33 main + 11 test files), Rule 26 `ResponseInfoFactory` static→instance adapt, Rule 41 `MainConfiguration` qualified as `esignMainConfiguration`, Rule 25 dropped `mockito-core:3.12.4` override in `domain-integration/pom.xml` so it inherits parent `5.7.0` (required to mock JDK-module `javax.xml.crypto.dsig.XMLSignatureFactory`), pipeline Gate 5 cross-platform fix (`shutil.which("mvn")` for Windows `mvn.cmd` / Unix `mvn`), `SERVICE_DEAD_KEYS` extended for `case`+`order` (matches `483b71f8c` pattern). C2 `bbfbc782b` = 12 contract DTOs Phase-35-lifted to `dristi-common/contract/esign/`, orphan `File` shadow-imports removed (`feedback_lifted_dto_shadowing` memory). **PR 2** (`monolith/esign-interceptor`, 1 commit `87b8aafba`): hand-port of `esign-interceptor` into `esign/internal/interceptor/` (`InterceptorService` + `InterceptorApiController` + `InterceptorServiceTest`), Rule 32 REST→direct (`InterceptorService` calls `ESignService.signDocWithDigitalSignature` directly), Rule 33 explicit `RequestInfo`, Rule 36 test mocks `ESignService`, Rule 38 `ServiceRequestRepository`/`callESign` dropped, `oAuthForDristi()` dropped (its OAuth bounce existed only to authenticate the now-eliminated REST hop — not audit-load-bearing). Configuration merged: only `redirectUrl` + `landingPageRedirectUrl` survive from interceptor's `Configuration`; `eSignHost`/`eSignEndPoint` + `oath*`/`dristhi.oath.*` (typo preserved upstream) intentionally not carried over. 4 orphan codegen models (Error/ErrorResponse/RequestInfoWrapper/ResponseInfo) dropped — never referenced in production. Verified: full reactor `mvn clean test` 1151 tests pass, all 8 pipeline gates green, BeanNameCollisionTest + ModuleStructureTest both pass. **Follow-ups**: bail-bond + digitalizeddocuments → esign REST→direct (`/v1/_getLocation`) deferred (needs `EsignApi` exposed first); FileStoreUtil lift to dristi-common (esign has 2 methods absent from canonical) deferred to its own Tier 3 PR; manual boot smoke at `/v1/_intercept` recommended before merge. |
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
