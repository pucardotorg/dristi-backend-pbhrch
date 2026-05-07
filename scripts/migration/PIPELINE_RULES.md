# Per-Module Migration Pipeline Rules

This is the working rule set used by the per-module migration pipeline
(`scripts/migration/per_module/run_module_migration.py`) and the
`dristi-common` extraction pipeline (`scripts/migration/dristi_common/`).

It supersedes the original 10-rule list in `agentic_workflow_april_27.md`
where reality required adjustments. Every "Refined by" note links the rule
to the concrete experience that produced it.

> Renamed from `SKILLS.md` to `PIPELINE_RULES.md` to disambiguate from
> Claude Code "skills" (a runtime feature). These entries are pure
> documentation — a debug archive — not invocable capabilities.

---

## Rule 1 — Package Naming Enforcer

**Rule.** Every Java file under `dristi-monolith/domain-<module>/` declares
`org.pucar.dristi.<module>.<subdomain>.internal.*`. Public API types live
one level up at `org.pucar.dristi.<module>.<subdomain>.*Api`.

**Enforcement.** `phase_7_validate` Gate 4 walks every file in the target
tree and fails if its `package` line doesn't start with the expected
`<target>.internal` prefix.

---

## Rule 2 — Duplicate Elimination Guard

**Rule.** The 11 protected classes
(`MdmsUtil`, `IdgenUtil`, `WorkflowUtil`, `FileStoreUtil`, `UserUtil`,
`UrlShortenerUtil`, `IndividualUtil`, `Producer`, `ServiceRequestRepository`,
`AuditDetails`, `ResponseInfo`) live in `dristi-common` only. Any local
copy in a module is a violation.

**Refined by reality.** The original doc assumed most copies were
byte-identical. They aren't — 10 of 11 protected classes are Cat B (4+
unique variants). So the destructive replace step is split:

- **Phase 4a (safe):** delete only copies whose hash matches the
  canonical. Leaves divergent copies in place with a tracker
  (`output/phase4a_divergent.csv`).
- **Manual converge:** reviewer reads the variants, optionally
  enhances the canonical to absorb a secondary cluster (template:
  `IdgenUtil` was extended with `isSequencePadded` and DRISTI-style
  exception handling so advocate / hearing / evidence / task can later
  drop their copies).

**Enforcement.** `phase_7_validate` Gate 2 fails if any protected class
file appears anywhere under the target tree.

---

## Rule 3 — Module Boundary Enforcer (Spring Modulith)

**Rule.** Code in `domain-X` may not import from `domain-Y.*.internal.*`.
Cross-module communication goes through `*Api` interfaces.

**Enforcement.** `dristi-app/src/test/java/.../ModuleStructureTest` calls
`ApplicationModules.of(DristiApplication.class).verify()`.

**Refined by reality.** Spring Modulith 1.1.6 is the version compatible
with Spring Boot 3.2.x — pinned in `dristi-app/pom.xml` not the parent
POM (the parent stays plain so child modules can opt out of modulith if
they're libraries-only).

---

## Rule 4 — REST-to-Method-Call Converter

**Rule.** Intra-DRISTI REST calls (`serviceRequestRepository.fetchResult`
or `RestTemplate.*` aimed at another DRISTI service) become direct
method calls on the target module's `*Api`. Calls to eGov/DIGIT
platform services stay as REST.

**Refined by reality.** Detection is automatable; conversion is not. The
shapes of cross-service calls differ too much (some pass `Object`, some
typed DTOs, some use builders). Phase 5 emits a per-service file listing
the intra-DRISTI candidates plus the host-getter method name; humans
convert each.

**Skip list (legitimate REST calls).** `EGOV_HOST_TOKENS` in the
pipeline: any host-getter whose name contains `egov`, `digit`,
`individual`, `user`, `mdms`, `filestore`, `idgen`, `workflow`,
`shortener`, `tracer`, `encryption`, or `keycloak` is treated as a
platform call and *not* flagged.

---

## Rule 5 — Kafka Topic Hygiene

**Rule.** Phase 1 keeps every topic that has a producer + consumer in
the new monolith *and* on the egov-persister side. Phase 2 removes
persistence-only topics as each module migrates to direct
`@Repository` writes.

**Refined by reality.** The `Producer` canonical was almost
re-implemented as a thin `KafkaTemplate.send` wrapper before the user
reminded us that `KafkaProducerService` carries production-critical
behaviour: structured success/failure logging and errored-payload
capture. Lesson: do not strip behaviour from a canonical without
explicitly asking. `KafkaProducerService` was extracted alongside
`Producer` into `org.pucar.dristi.common.kafka`.

---

## Rule 6 — Persistence Pattern Guard

**Rule.** Phase 1 — all writes go via `Producer → topic →
egov-persister`. Phase 2 — migrated modules use `@Repository` +
`@Transactional`. No mixing.

(Unchanged from the original doc; not exercised yet.)

---

## Rule 7 — Configuration Consolidation Guard

**Rule.** Only `dristi-app/src/main/resources/application*.yml` exists
in the monolith. Module subprojects must not contain
`application*.yml` (test resources are fine).

**Enforcement.** `phase_7_validate` Gate 3 checks for stray
`application*.yml` under `domain-<module>/src/main/resources/`.

**Refined by reality.** Service-specific Spring `Configuration` classes
(`@Value("${egov.idgen.host}")` and friends) cannot all live in
dristi-common — `case`'s class alone is 499 lines of service-specific
properties. The right split: extract the union of properties used by
`dristi-common` canonical classes into `CommonConfiguration` (currently
~18 properties); each service keeps its own service-internal
`Configuration` for the rest.

---

## Rule 8 — Import Sanitizer

**Rule.** Inside `dristi-monolith/domain-*/`, banned import prefixes are:
`digit.`, `pucar.`, `notification.`, `drishti.payment.`,
`com.egov.icops`, `com.dristi.njdg`, `org.egov.eTreasury.`,
`org.egov.transformer.`, `org.drishti.esign.`, `com.pucar.drishti.`.

**Enforcement.** `phase_7_validate` Gate 1.

---

## Rule 9 — Automated Package Rename Engine

**Phase 3 of the per-module pipeline.** Reads
`migration_manifest.json`, walks the source service's `src/main/java`,
copies each file into `dristi-monolith/domain-<module>/.../<subdomain>/internal/`
with `package` and `import` lines rewritten. The
`@SpringBootApplication` entry point is *not* copied (only `dristi-app`
has one).

**Refined by reality.**

1. **HAND-CURATED guard.** Re-running the pipeline on a service whose
   target tree has been hand-edited would silently overwrite the human
   work. The rewrite step now skips any destination file whose first
   ~120 chars contain `// HAND-CURATED`. Same guard applies in Phase 6
   (test migration).
2. **JDK precondition.** Maven needs a JDK with `javac`, not just the
   JRE. `ensure_jdk_17()` finds a Corretto-17 install on disk if `javac`
   isn't on PATH and exports `JAVA_HOME`/`PATH` for the subprocess
   `mvn` calls.
3. **Lombok annotation processing in this repo is unreliable.** New
   classes added to `dristi-common` (e.g. `CommonConfiguration`) use
   hand-written getters/setters. This avoids "cannot find symbol
   getXxx" errors that come from intermittent annotation-processor
   discovery in Spring Boot 3.2.2 + maven-compiler-plugin 3.11.0.
4. **Version pin blocklist on the parent POM.** Some legacy services
   pin `spring-kafka 2.7.8`, `spring-core 5.3.x`, etc. — these would
   override Spring Boot 3.2.2's BOM. The parent-POM generator
   (`scripts/migration/scaffold/01_generate_parent_pom.py`) skips
   pinning `org.springframework.kafka:spring-kafka` and the core
   `org.springframework:spring-{context,core,web}` artifacts so Boot's
   BOM wins.

---

## Rule 10 — Schema Isolation Guard (Phase 2)

**Rule.** Each module's `@Entity`/`@Table` declares an explicit
`schema=` matching its domain (`case_lifecycle`, `identity`,
`integration`, `financial`).

(Not exercised yet; deferred to Phase 2 of the migration.)

---

## Rule 11 — Test Migration & Verification Guard

**Rule.** `src/test/java` for the migrated service is copied to the
target module's `src/test/java/.../internal/` with the same package
rewrite applied. Tests that mock `ServiceRequestRepository` for an
intra-DRISTI call need their mock retargeted to the direct service
call.

**Refined by reality.** The HAND-CURATED guard applies to test files
too — once a reviewer has fixed up a test's mocks, re-running the
pipeline will not undo that work.

---

## Rule 23 — Per-Controller Context-Path Prefix

**New rule, learned the moment a second service joined the monolith.**

Spring Boot supports exactly **one** `server.servlet.context-path` per
application. While the monolith hosts only lock-svc the
`server.servlet.context-path: /lock-svc` setting in
`dristi-app/application.yml` worked. As soon as case migrated, every
case endpoint started 404'ing because `/case/v1/_search` was being
served from `/lock-svc/v1/_search`.

The fix: **drop the global context-path** and make each controller carry
its own URL prefix on the class-level `@RequestMapping`.

Phase 3 of `run_module_migration.py` now does this automatically:
1. `_read_source_context_path` extracts the original
   `server.servlet.context-path` (or `server.contextPath`) from the
   service's source `application.properties`. Stored in the migration
   manifest as `source_context_path`.
2. `_prefix_controller_paths` walks every `@RestController` /
   `@Controller` in the migrated tree, locates the class-level
   `@RequestMapping`, and prefixes its value with the original
   context-path. Idempotent — re-running on an already-prefixed file
   is a no-op.
3. If a controller has no class-level `@RequestMapping`, one is
   inserted above the class declaration.

After this rule, both `/lock-svc/v1/_set` and `/case/v1/_search` work
out of the box; the caller URL contract is preserved at every gateway
remap point (no UI / Postman changes).

---

## Rule 22 — Config Consolidation Engine (Pipeline 5)

**New pipeline phase, learned from the case-svc boot attempt.**

Each migrated service has its own `application.properties` — case alone has
217 keys feeding 148 `@Value` bindings. The monolith's bootstrap
`application.yml` can't satisfy all of them by hand, and Spring fails
fast on any unresolved `${placeholder}`.

Pipeline 5 (`scripts/migration/config_consolidation/run_consolidation.py`)
takes a list of services, parses each `application.properties`, and
classifies every key:

| Classification | Where it lands |
|---|---|
| **SHARED** — same value across all services that have it | `dristi-app/src/main/resources/application-shared.yml` |
| **SERVICE-SPECIFIC** — only one service has the key | `domain-<module>/src/main/resources/application-<subdomain>.yml` |
| **CONFLICT** — different values across services | per-subdomain yml of each service; profile-overlay order resolves |

Per-subdomain YMLs live in the **owning domain module**'s resources
(not at the bootstrap). Spring Boot scans `application-<profile>.yml`
from anywhere on the classpath, so each domain module ships its own
config — case's properties travel with `domain-case-lifecycle`,
lock-svc's with the same module, etc.

Drop-list (keys the monolith decides, not the service):
- `server.port`, `server.contextPath`, `server.servlet.context-path`
- `spring.datasource.*`, `spring.flyway.*`, `spring.kafka.*`,
  `spring.jpa.*` (single DB, single Kafka, single Hibernate config)
- `otel.service.name`, `logging.loki.app` (single OTel + Loki app name)
- `spring.application.name`, `spring.profiles.active`,
  `management.endpoints.web.base-path`

Activation (in `dristi-app/.../application.yml`):
```
spring:
  profiles:
    active: shared,cases,locksvc,local
```

Output of running on case + lock-svc:
- 192 keys consolidated
- 26 SHARED (one shared yml)
- 4 CONFLICTs (each subdomain yml carries its own value)
- 162 SERVICE-SPECIFIC (split across the two subdomains)

After running, `mvn package` succeeds and the monolith parses every
`@Value` placeholder cleanly — only environmental issues (DB creds)
remain at boot.

---

## Rule 14 — Inline FQN Rewriter (learned from `case`)

**New rule, surfaced by the case service test drive.**

Some legacy code uses fully-qualified class names inline rather than via
`import`:

```java
public org.pucar.dristi.web.models.ProcessInstance getCurrentWorkflow(...) { ... }
private final org.pucar.dristi.enrichment.AdvocateDetailBlockBuilder builder;
```

Phase 3's package rewrite previously only handled `package` and `import`
statements, leaving inline FQNs broken after migration. The rewrite now
also walks every `<currentPkg>.<seg>.` reference in the body, with
`<seg>` checked against `DOMAIN_MODULE_SEGMENTS` (`common`,
`caselifecycle`, `identity`, `integration`, `payments`) — references
into other modules pass through unchanged.

---

## Rule 15 — Arity-Aware Method-Surface Comparison

**New rule, learned from the case service.**

`Rule 13`'s naïve approach compared protected-class method *names*
only. Case's local `IndividualUtil.getIndividualByIndividualId(req, uri)`
had the same name as the canonical's
`getIndividualByIndividualId(req, uri, Class<T>)` but a different arity,
so the local was deleted and callers stopped compiling.

The fix: `_public_methods()` now returns `(name, parameter-count)`
tuples. Different arities produce different surface entries — case's
local IndividualUtil is now correctly KEPT as a follow-up because its
two-arg overload doesn't exist on the canonical.

---

## Rule 16 — Banned Imports Need a Whitelist

**New rule, surfaced by the case service test drive.**

The original "banned import prefixes" list was too coarse — `digit.`
caught both the legacy DRISTI service-internal `digit.config.*` AND the
legitimate external library `digit.models.coremodels.*` (from
`org.egov.services:digit-models`). After the case-service run reported
seven false-positive Gate-1 hits, the list was tightened to enumerate
the real service-internal subpackages
(`digit.config.`, `digit.repository.`, `digit.service.`, `digit.util.`,
`digit.web.`, `digit.kafka.`, `digit.enrichment.`,
`digit.validators.`, `digit.scheduling.`, `digit.annotation.`)
plus the legacy `pucar.*` subpackages and explicit external service
roots (`org.egov.eTreasury.`, `com.egov.icops`, etc.).

`digit.models.*` and `org.egov.common.*` flow through cleanly.

---

## Rule 17 — eGov Platform Skip List Is Repository-Wide

**New rule, surfaced by the case service test drive.**

Phase 5's intra-DRISTI REST detector skips host-getters whose names
contain platform tokens (so the eGov platform calls stay as REST). The
case service revealed two missing tokens — `billing` and `hrms` — both
of which point at egov platform services, not DRISTI services. The
skip list was extended to cover them. Updating the list is cheap;
re-running Phase 5 on a service that contains a previously-flagged
genuine intra-DRISTI candidate will re-emit it after the change.

---

## Rule 18 — Canonical Method Surface Must Stay Generic Where Service DTOs Diverge

**New rule, learned from the case service test drive.**

The canonical `IndividualUtil` originally took the
`org.pucar.dristi.common.models.individual.IndividualSearchRequest`
type. Case has its own `IndividualSearchRequest` with extra fields,
incompatible with the common shape. The four
`Individual*` methods on the canonical were widened to accept `Object`
for the request — the canonical only inspects the JSON response shape,
not the request. This pattern (widen to `Object` when service DTOs
diverge but the JSON wire format is fixed) is the right call whenever
canonical methods would otherwise force every service to migrate its
DTO before adopting the canonical.

---

## Rule 19 — `kept_classes` Must Suppress BOTH Import-Rewrites AND Auto-Imports

**New rule, learned from the case service test drive.**

Rule 13 introduced "keep local protected class as follow-up" but
applied that flag only when deciding whether to delete the file.
Phase 4's import-rewrite block still redirected
`org.pucar.dristi.caselifecycle.cases.internal.util.FileStoreUtil`
imports to `org.pucar.dristi.common.util.FileStoreUtil` — even though
case's local `FileStoreUtil` was kept (it has `saveDocumentToFileStore`
that the canonical lacks). Callers like `CasePdfService` then failed
to compile because the canonical doesn't expose that method.

The fix: Phase 4 now skips kept classes both in the import-rewrite
loop and in the auto-import-insertion sweep. Local references continue
to resolve to the kept local file; callers compile against the methods
the file actually has.

---

## Rule 20 — Phase Order: 6 Before 4

**New rule, learned from the case service test drive.**

Phase 4's import-rewrite + auto-import sweep needs to walk both the
main and test trees. Originally Phase 4 ran before Phase 6, so the
test tree didn't exist and tests retained stale imports / lacked
auto-inserted dristi-common imports.

Reordered: `1, 2, 3, 6, 4, 5, 8, 7`. Phase 6 now does only the package
rename when copying tests; Phase 4's sweep covers both trees with
consistent state.

---

## Rule 21 — Spring Modulith: Filter Style Violations

**New rule, learned from the case service test drive.**

Spring Modulith's `verify()` runs both structural rules (boundaries,
cycles, named-interface respect) and stylistic rules (e.g.
"prefer constructor injection over field injection"). The 48 case-svc
classes that use `@Autowired` on fields all flag the latter.

`ModuleStructureTest` now calls `detectViolations()` and filters out
lines containing the style-only hints (`uses field injection`,
`Prefer constructor injection`). Real boundary or cycle violations
still fail the build. The kept-but-filtered violations are tracked
separately for cleanup.

---

## Rule 13 — Service-Augmented Helper Detection

**New rule, learned from the lock-svc test drive.**

Some services attach service-specific convenience methods to a protected
utility class (lock-svc's `IndividualUtil` adds `getIndividualId(RequestInfo)`
and `getIndividual(...)` on top of the generic `getIndividualByIndividualId`).
Phase 4 of the per-module pipeline now compares the local file's public
method surface to the canonical's:

- If the local file is a strict subset → delete (existing behaviour).
- If the local file has methods absent from the canonical → KEEP it in
  the target tree and append a row to
  `scripts/migration/per_module/output/<service>_followups.txt`.

Gate 2 of Phase 7 reads that follow-ups file and exempts the listed
files from the "no duplicates" rule. The reviewer decides whether to
lift the extra methods into dristi-common or rename the local file to
`<Subdomain><Class>Helper` and keep it module-local.

---

## Rule 24 — Eager Contract DTO Lift to `dristi-common/contract/<subdomain>/`

**New rule, learned from the order service migration.**

Order's REST callers (e.g. `CaseUtil` → case service, `AdvocateUtil` →
advocate service) need typed DTOs to convert from `RestTemplate` calls
to direct method calls. Each service used to keep its own copy of those
DTOs in `web/models/`, so the caller's `OrderResponse` and the callee's
`OrderResponse` were *different types* — converting needed boilerplate
mappers, and shapes drifted over time. Doing the lift as a follow-up PR
also doubles the test cycle (one PR for the structural lift, another
for the contract lift) for every service.

**Rule.** Phase 35 (contract-lift; CLI ID `35` is the alias for the conceptual "Phase 3.5") lifts a service's contract surface
into `dristi-common/src/main/java/org/pucar/dristi/common/contract/<subdomain>/`
during the migration itself. A class is in scope if any of:

1. Its simple name ends with `Request`, `Response`, `Criteria`,
   `SearchCriteria`, `Wrapper`, or `Payload`. (Suffix list is
   conservative — `Envelope` was dropped after a grep showed zero
   occurrences in the source services.)
2. It is referenced as a controller `@RequestBody` parameter, or as the
   payload type of `ResponseEntity<X>` / a controller method's return
   type.
3. It is transitively referenced — as a field type, generic argument,
   `extends`, or `implements` clause — from any class already in the
   lift set, **AND** lives in the same `web/models/` tree. The closure
   stops at non-local packages (`org.egov.*`, `java.*`, etc.); cycles
   terminate via a visited set.

**Enforcement.**

- `phase_3_5_contract_lift` moves matching files, rewrites the package
  declaration, stamps `// HAND-CURATED` so re-runs preserve human edits,
  and returns a `lift_map` of `old_fqcn -> new_fqcn`.
- The same function sweeps every `domain-*/src/{main,test}/java` tree
  for stale imports, including wildcard imports of `<svc>.web.models.*`
  (it adds a parallel wildcard for the contract package rather than
  expanding each named class).
- Auto-import insertion for same-package simple-name references is
  scoped to files **inside the migrating subdomain only** — without
  this, a sibling subdomain that has its own `Advocate` would gain a
  stray `import <other-subdomain>.contract.<...>.Advocate` and fail to
  compile.
- Phase 35 writes a `package-info.java` in
  `dristi-common/contract/<subdomain>/` that declares
  `@NamedInterface("contract-<subdomain>")`. Without this Spring
  Modulith treats every lifted DTO as internal-to-`common` and
  `ModuleStructureTest` fails with "depends on non-exposed type"
  for every cross-module reference. The marker is idempotent on
  re-runs (skipped if the file already exists, so hand-edits to the
  Javadoc survive).
- Phase 4's import-rewrite sweep also applies the `lift_map`, so any
  imports that surfaced after Phase 6 copied the test tree get fixed.
- Gate 8 in `phase_7_validate` fails if any contract-suffixed class
  remains under `<subdomain>/internal/web/models/`.

**Side effects worth knowing.**

- Lifted DTOs drag their compile-time deps (e.g. `javax.validation.*`,
  `io.swagger.annotations.*` from Swagger 1.x) into `dristi-common`.
  The pom carries `digit-models` and `swagger-core:1.5.18` to satisfy
  these legacy namespaces. Drop both once the codebase migrates to
  Jakarta + Swagger 2.x.
- Per-subdomain namespacing prevents collision when two services have
  same-named DTOs (`order/Document` vs `case/Document`). Promotion to
  shared `dristi-common/contract/<shared>/` is a deliberate follow-up,
  not a migration step — wait until two services prove they want the
  same concrete type.
- `case` and `lock-svc` were migrated before Phase 35 existed; their
  contract DTOs are still under `internal/web/models/`. A retro-lift
  PR is owned separately. **Note:** the case retro-lift was attempted
  in the LockApi/CaseApi cutover PR and rolled back after Phase 35
  produced 203 compile errors — see [Rule 24a](#rule-24a--namedinterfacecontract-when-retro-lift-isnt-feasible)
  for the alternative pattern, and [FOLLOWUP_RETROLIFT_PATH_A.md](FOLLOWUP_RETROLIFT_PATH_A.md)
  for the deferred Path A details.

---

## Rule 24a — `@NamedInterface("contract")` When Retro-Lift Isn't Feasible

**New rule, learned from the LockApi/CaseApi cutover PR.**

Rule 24's "lift contract DTOs to `dristi-common/contract/<subdomain>/`"
is the preferred shape. It doesn't always work: services with
**tangled `web/models/` trees** produce a class of errors Phase 35
can't resolve mechanically:

| Tangle category | Concrete example |
|---|---|
| **JPA + DTO dual role** | `Lock` is `@Entity(name="lock")` AND the wire DTO. After lift to `dristi-common`, `jakarta.persistence` isn't on dristi-common's classpath; `LockRepository extends JpaRepository<Lock, UUID>` still needs the entity at its old location |
| **Subpackage references** | `Address.java` imports `cases.internal.web.models.v2.AddressV2`. Phase 35 walks only top-level `web/models/*.java`; subpackage types stay put. Lifted `Address` can't resolve `AddressV2` from `dristi-common` |
| **Internal annotation references** | `CaseSearchCriteria` uses `cases.internal.annotation.SomeValidator` (custom Bean Validation). Lifted DTO references a class in another module |
| **External library deps not in dristi-common pom** | `IndividualSearch` uses `org.egov.common.data.query.annotations.*`; that artifact isn't pulled in |

Concrete data: running Phase 35 on case lifted 140 classes and produced
**203 compile errors** spanning all four categories. Rolling Phase 35
forward to handle these is open-ended — each fix may surface new
categories. Path A (proper retro-lift) is deferred; Path B (in-place
exposure) unblocks today.

**Rule.** When Rule 24's retro-lift isn't feasible — measured by a
trial Phase 35 run failing to compile — keep contract DTOs in
`<subdomain>/internal/web/models/` and stamp the package with
`@NamedInterface("contract")`:

```java
// <subdomain>/internal/web/models/package-info.java
@org.springframework.modulith.NamedInterface("contract")
package org.pucar.dristi.<domain>.<subdomain>.internal.web.models;
```

Cross-subdomain callers import `<subdomain>.internal.web.models.<Type>`
directly. Spring Modulith's `verify()` accepts the import because the
named-interface marker exposes the package; without the marker it
would fail with "depends on non-exposed type."

**Decision tree.**

| Service shape | Pattern |
|---|---|
| Flat `web/models/` (only top-level `.java` files), no JPA dual-role, no internal-annotation refs | **Rule 24** — lift to `dristi-common/contract/<subdomain>/` |
| Tangled `web/models/` (any of the 4 categories above) | **Rule 24a** — keep in place, expose via `@NamedInterface("contract")` |

The two patterns coexist; both produce a clean cross-module boundary.
The difference is *where* the contract types physically live.

**Trade-off for choosing Rule 24a now.** Rule 24a defers the canonical
relocation to `dristi-common`. Cost of moving from 24a → 24 later is
bounded: Phase 35's import-rewrite sweep handles caller-import
migration automatically, the `@NamedInterface` marker comes off in
one line, and the deeper work (splitting dual-role entity/DTO classes,
robustifying Phase 35) is the same magnitude regardless of timing.
The followup is tracked in [FOLLOWUP_RETROLIFT_PATH_A.md](FOLLOWUP_RETROLIFT_PATH_A.md).

**Enforcement.** Same as Rule 24 — Spring Modulith's
`ModuleStructureTest.verify()` is the gate. Cross-subdomain references
to types not behind a named interface fail the test.

---

## Rule 25 — Parent POM Dependency Hygiene

**New rule, learned from the order service migration.**

The monolith's parent pom (`dristi-monolith/pom.xml`) pinned
`mockito-core:3.12.4` while every source service uses `5.7.0` (via
Spring Boot's BOM). Most migrations didn't notice — until order
brought in tests that mock `JsonNode` (a Jackson abstract class).
Mockito 3.12 + jackson 2.15+ raised
`WrongTypeOfReturnValue: Boolean cannot be returned by getNodeType()`
because Mockito 3.x's stub-capture path delegates through the JsonNode
final-method chain in a way 5.x fixed.

**Rule.** When the parent pom pins a transitive dep that the source
services already use at a different version, **align with the source
version** rather than the parent's pin. Specifically, the test
toolchain (`mockito-core`, `mockito-junit-jupiter`) must be one
consistent version; mixing 3.x core with 5.x junit-jupiter is the
specific failure mode that bit order.

**Enforcement.** None automated — the parent pom is hand-curated. Treat
this as a checklist for any migration PR whose tests touch
`mock(<abstract-class>.class)`. If you need to bump a parent-pom
version, also propagate it into the scaffold script
(`scripts/migration/scaffold/02_generate_module_skeletons.py`) so
future regenerations carry the bump forward — otherwise the next
scaffold rebuild will silently revert it.

**Followup.** Audit the parent pom for other stale pins (postgres,
flyway, jackson) before they bite the next service.

---

## Rule 26 — Canonical Method Return-Type Drift

**New rule, learned from the order service migration. Companion to Rule 18.**

Rule 18 covered request-side divergence (widen the canonical to
`Object` when service DTOs differ). Order surfaced the *return-side*
case: the canonical `MdmsUtil.fetchMdmsData(...)` returns
`Map<String, Map<String, JSONArray>>` (parsed) while order's source
copy returned `String` (raw JSON). Phase 4 deduplicated the local copy
in favor of the canonical — correct — but order's call sites still
expected the `String` shape and failed to compile.

**Rule.** When Phase 4 deletes a local protected-class copy in favor
of the canonical, check whether call sites compile against the
canonical's signature. If not, the **caller** adapts to the canonical
— never widen the canonical to `Object` for return types (callers
would lose all type safety).

**Concrete adaptation patterns** (from the order migration):

1. **Pattern A — direct usage of parsed structure** (3 sites in
   `MdmsDataConfig`): replace
   `String s = util.fetch(...); Response r = mapper.readValue(s, Response.class); array = r.getMdmsRes().get(m).get(n);`
   with `Map<String,Map<String,JSONArray>> rs = util.fetch(...); array = rs.get(m).get(n);`. Drops a redundant marshal/unmarshal hop.

2. **Pattern B — JsonPath against the raw string** (1 site in
   `OrderRegistrationValidator`): re-serialize the parsed map back
   into JSON with a wrapper so the existing JsonPath config keeps
   working: `String json = mapper.writeValueAsString(Collections.singletonMap("MdmsRes", rs));`. Avoids touching the JsonPath config.

**Enforcement.** None automated; surfaced as compile errors during
Phase 7 / `mvn verify`. RUNBOOK §7 has the symptom row pointing here.

---

## Rule 27 — REST → Direct Calls Are Follow-up PRs, Not Migration PRs

> **Superseded by [Rule 32](#rule-32--restdirect-converts-at-target-migration-time-not-caller-migration-time).**
> Rule 27 was the right call when API-first infrastructure didn't exist
> yet. With per-subdomain `@ApplicationModule` (Rule 31) and `*Api`
> interfaces in place, REST→direct conversion belongs in the *target's*
> migration PR rather than a separate follow-up. Original text retained
> below for archive.

**Original (now superseded), learned from the order service migration.**

When a service `S` is migrated, Phase 5 emits a list of intra-DRISTI
REST callers in `<service>_rest_calls.txt`. Tempting to convert
straightforward `@Autowired` swaps in the same PR — but the
conversion is a **behavioral** change (different error semantics,
different deserialization path, different serialization timing), not
a structural lift. Mixing changes the blast radius of the migration
PR from "did we move files correctly?" to "did we move files AND
preserve runtime behavior?".

**Rule.** Each service migration PR is structural-only. REST → direct
conversions land as separate, focused PRs after the migration's
target services are in the monolith.

**Decision tree** (from /migrate-service Step 3):

| Caller's target | Action in migration PR |
|---|---|
| Migrated DRISTI service | Leave as REST. Follow-up PR converts. |
| Unmigrated DRISTI service | Leave as REST. Will need conversion when target migrates. |
| Platform (eGov / DIGIT — see `EGOV_HOST_TOKENS`) | Leave as REST. Permanent. |

**Exception.** A single straightforward conversion (typed DTO in/out,
target already migrated, no `JsonNode` / `Map.class` indirection)
that is genuinely behaviour-preserving may go in the migration PR if
the migration author judges the risk low. Default is "follow-up";
the migration author owns the call.

**Enforcement.** None automated. RUNBOOK §5.1 documents the
decision tree.

---

## Rule 28 — Three-Commit Structure per Migration PR

**New rule, learned from the order service migration.**

The order PR mixed structural lift, contract uplift (Phase 35 was
being authored concurrently), and pipeline-source changes into one
working tree. Reviewing the diff is harder than it needs to be: a
behavioural regression can't be bisected cleanly between "did we move
files correctly" and "did the contract lift change anything subtle".
Separating concerns into commits keeps each one small and gives a
clean revert boundary.

**Rule.** From `hearing` onwards, every migration PR carries three
commits in this order:

1. **`migrate(<svc>): structural lift to domain-<module>/<subdomain>`**

   Pure file move from `dristi-services/<svc>` →
   `dristi-monolith/domain-<module>/.../<subdomain>/internal/`. Includes
   manual Tier 1 fixes that are part of the lift mechanics (Flyway
   `_2` rename for Gate 7, caller adaptation to canonical signature
   per Rule 26, Spring profile wiring, dep version bumps in the
   parent pom per Rule 25). No contract DTO moves; the `web/models/`
   tree stays under `internal/` at this commit.

2. **`refactor(<svc>): contract uplift + REST→direct calls`**

   Phase 35's effects: contract DTOs moved to
   `dristi-common/contract/<sub>/`; caller imports rewritten in
   `<sub>/internal/`; `dristi-common/pom.xml` deps added if needed
   (e.g. `digit-models`, `swagger-core:1.5.18`). Plus REST→direct
   conversions for any target service that's already in the monolith
   AND has its own contract DTOs already lifted (per Rule 27's
   decision tree). If the only callable target is unmigrated, this
   commit is just the contract uplift.

3. **`feat(pipeline): <change>`** *(optional)*

   Edits to `run_module_migration.py`, new gates, new rules in
   `PIPELINE_RULES.md`, doc updates in `RUNBOOK.md` / `CLAUDE.md`.
   Skip if the migration didn't motivate any pipeline/rules change.

**How to actually achieve it:**

The standard entrypoint is the `/migrate-service` slash command —
[.claude/commands/migrate-service.md](../../.claude/commands/migrate-service.md)
— which orchestrates the same phases with pre-flight checks and
manual-review gating built in. The raw flags below are what
`/migrate-service` invokes under the hood; reach for them only when
running outside a Claude session.

1. Branch from `monolith/main`.
2. Structural lift (commit C1):
   ```bash
   /migrate-service <name> <module> <subdomain>          # preferred
   # or, raw:
   python3 scripts/migration/per_module/run_module_migration.py \
     --service <name> --module <module> --subdomain <subdomain> \
     --phase 1,2,3,4,5,6,7,8,9
   ```
   Apply manual fixes. Verify `mvn test -pl domain-<module>`.
3. Contract uplift + REST→direct (commit C2):
   ```bash
   python3 scripts/migration/per_module/run_module_migration.py \
     --service <name> --module <module> --subdomain <subdomain> \
     --phase 35
   ```
   Add any required `dristi-common` deps. Convert straightforward REST
   callers per Rule 27. Verify build.
4. (Optional, commit C3) If this migration motivated pipeline / rule
   changes, edit them now. Otherwise, skip.

**Why this beats one bundled commit:**

- **Bisect:** a runtime regression isolates cleanly to "structural"
  vs "behavioural" vs "pipeline-only".
- **Reviewer fatigue:** structural lift is a bulk move with low
  cognitive load; contract uplift + REST switch is a focused
  behavioural review.
- **Revert:** if the REST conversion misbehaves in QA, revert C2
  alone — C1's structural lift stays in.

**Why this is cheap to follow from `hearing` onwards:** Phase 35 is
in main now. The natural sequence is "lift first without `--phase 35`,
commit, then `--phase 35`, commit" — the work is already serialised in
that order; the only discipline is splitting the commit boundary.

**Exception.** The order migration (this PR) was bundled into two
commits because Phase 35 was being authored mid-PR. The 3-commit
structure is the default from `hearing` onwards.

**Enforcement.** None automated; reviewer discipline. Could be linted
in CI later; not blocking for now.

---

## Rule 29 — Workflow Migration Pattern (and the Behavior-Union Lesson)

**New rule, learned from the order service migration.**

The original canonical `WorkflowUtil` was extracted from `ab-diary` (the
first-found variant). It missed `setDocuments`, `setAdditionalDetails`,
and returned `state.getApplicationStatus()` instead of `state.getState()`.
Order's caller `if (PUBLISHED.equalsIgnoreCase(status))` silently never
matched, the workflow service rejected transitions for missing
`documents`, and `additionalDetails` were dropped on every transition.

The rule has two parts: a process rule (how to extract canonicals) and
a workflow-specific shape (what the canonical surface should be).

### 29A — Extract canonicals from the BEHAVIOR UNION, not first-found

**Rule.** Phase 4 of the `dristi-common` extraction picks a "majority
variant" canonical based on file-hash plurality. That picks the most
common *body* but doesn't see *behavior outliers* that need preservation.
Before locking a canonical, audit every observed service's variant of
the class for:

1. Field setter calls the canonical doesn't make (`setDocuments`,
   `setAdditionalDetails`, etc.)
2. Return-value differences (`state.getState()` vs
   `state.getApplicationStatus()`)
3. Method overloads or extra public methods

The canonical must be the **union** of all observed behaviors, exposed
via overloads / sibling methods so each caller keeps its semantics. Phase
4's signature-only detector (`(name, param-count)` tuples) cannot see
body-level divergence; it deletes the local copy whenever the signature
matches and silently introduces regressions like the three above.

**Enforcement.** No automated detector yet — this is reviewer
discipline. RUNBOOK §5 manual-review checklist now includes a step:
"audit any class in `PROTECTED_CLASSES` whose source body differs
across services". The migration author runs the audit before merging
and either widens the canonical or marks the local with the
`// SERVICE-AUGMENTED` opt-out (deferred — not yet implemented; for
now the audit catches it).

### 29B — Workflow-specific canonical shape

The canonical `WorkflowUtil` is now the workflow toolkit:

| Method | Returns | Use when |
|---|---|---|
| `updateWorkflowStatus(...)` | `state.getState()` | Caller compares against state-machine names ("PUBLISHED", "DRAFT") |
| `updateWorkflowApplicationStatus(...)` | `state.getApplicationStatus()` | Caller compares against egov-defined application lifecycle |
| `getProcessInstanceForWorkflow(...)` | `ProcessInstanceObject` | Service-local code building a transition request |
| `callWorkFlow(ProcessInstanceRequest)` | `State` | Service-local code that built its own request and just needs the call |
| `getWorkflow(List<ProcessInstance>)` | `Map<String, WorkflowObject>` | Search response → WorkflowObject map |
| `getWorkflowFromProcessInstance(ProcessInstance)` | `WorkflowObject` | Single search result conversion |
| `getUserListFromUserUuid(List<String>)` | `List<User>` | Stub User objects with UUID set |
| `getBusinessService(...)` | `BusinessService` | Look up workflow config |

Public surface uses **DRISTI's `WorkflowObject` / `ProcessInstanceObject`**
(the egov extension that adds `additionalDetails`). Egov's plain
`Workflow` / `ProcessInstance` only appears at the boundary with the
egov-workflow service (request payload, response shape).

### 29D — `additionalDetails` round-trip is one-way today

The forked `digit-services/egov-workflow-v2` understands
`additionalDetails` on its internal `org.egov.wf.web.models.ProcessInstance`
— so DRISTI services can SEND the field to workflow successfully (request
direction works). The RESPONSE direction silently drops the field:
DRISTI clients deserialise responses into
`org.egov.common.contract.workflow.ProcessInstance` from the
`services-common` jar, which has no `additionalDetails`. Jackson drops
unknown JSON properties.

If a future caller needs the field on the inbound side, the fix is one of:

- Publish the fork's DTO classes as an upgraded `services-common`
  artifact and bump the dep in `dristi-common/pom.xml`.
- Add a custom `ProcessInstanceObjectResponse` to dristi-common with
  `List<ProcessInstanceObject>` so Jackson deserialises directly into
  the subclass.

Until then, the canonical's `getWorkflowFromProcessInstance` only
recovers `additionalDetails` via an `instanceof ProcessInstanceObject`
check — true when the caller constructed the object in-memory, false
when they got it from a workflow response.

### 29C — Rule for future migrations of services with WorkflowUtil/WorkflowService

When migrating a service that has `WorkflowUtil` and/or `WorkflowService`:

1. **Drop service-local `WorkflowObject` / `ProcessInstanceObject`.**
   The shared types live at `dristi-common.models.workflow.*` (added
   to `PROTECTED_CLASSES` so Phase 4 auto-redirects imports).
2. **`WorkflowUtil` callers** map to canonical's
   `updateWorkflowStatus` (state-name return) or
   `updateWorkflowApplicationStatus` (app-status return) per the
   service's existing semantics. Audit the workflow YAML if unsure
   — most callers want state-name. Phase 4 deletes the local
   `WorkflowUtil`; verify no body-divergence regressions before merge
   (cf. 29A audit).
3. **`WorkflowService` stays service-local.** It's where the
   domain-specific business logic lives: businessService picking
   (case has hardcoded "ADV" + payment routing; application has
   `getBusinessName(svc)` switch; evidence has `getBusinessServiceName`
   based on `filingType`/`artifactType`); domain entity → ProcessInstance
   field extraction; domain-specific role checks
   (`isDelayCondonationCreator`, `isCitizen`); payment-specific
   helpers.
4. **Service's `WorkflowService` should delegate generic operations to
   canonical:** `callWorkFlow`, `getProcessInstanceForWorkflow`,
   `getWorkflowFromProcessInstance`, `getUserListFromUserUuid`. Drop
   the duplicated bodies; inject `WorkflowUtil` and call the canonical.
5. **`getCurrentWorkflow` typically stays service-local** — most
   services define their own `ProcessInstance` subclass at
   `web/models/ProcessInstance.java` (separate class, not a subclass
   of egov's), and the response deserialises to that local type. The
   canonical can't generalise over service-specific local types.

**Enforcement.** Manual review per migration. The migration recipe
in [/migrate-service](.claude/commands/migrate-service.md) Step 5 now
asks the migration author to confirm WorkflowUtil/WorkflowService
audit when the service has either class.

---

## Rule 30 — Pre-commit Summary Protocol

**New rule, learned from the order service migration.**

Migration PRs accumulate dozens of file changes across heterogeneous
concerns: structural lift, contract uplift, canonical promotion,
caller rewrites, pipeline-source edits, rules + docs. Committing
without surfacing the change-set first makes the PR review opaque
and any follow-up amend churn-y. Verify-before-commit (Rule N/A —
session-level convention) handles correctness; this rule handles
*reviewability*.

**Rule.** Before running `git add` / `git commit` on a migration
session, surface a structured summary and **wait for the user to
confirm**. The summary has four sections:

1. **Files added** — table of `path | purpose`. One line per file.
2. **Files deleted** — table of `path | reason`. Make the reason
   explicit (e.g. "redundant with shared", "lifted by Phase 35").
3. **Files modified** — table of `path | one-line change`. Group by
   concern (canonical / pipeline-source / domain code / tests / docs)
   when the count exceeds ~10.
4. **Decisions taken** — table of `decision | rationale`. Captures
   non-obvious choices (e.g. "two return-flavor methods instead of a
   boolean flag — self-documenting at call site") so the user can
   challenge the design before it's locked in a commit. Decisions
   that came from explicit user direction earlier in the session
   should reference that direction.
5. **Verifications NOT yet run** — bulleted list of checks the user
   should expect before commit (per the verify-before-commit rule).
   The summary itself is paused-on-user; verifications run AFTER
   user confirms the design, not before.

After the user confirms:
- Run all the listed verifications.
- Surface failures with the same precision; never commit through red.
- Only then run `git add` / `git commit`.

**Why before verifications, not after.** The verifications cost time
(mvn runs) but the user can spot a wrong-turn decision in 30 seconds.
Surfacing the summary first lets the user redirect *before* you spend
3 minutes verifying a design they'd reject.

**When this rule does NOT apply.** Trivial single-purpose commits
the user explicitly authorised mid-session (`/migrate-service` step
6's "ship it" prompt covers this — its summary already follows this
shape). The rule kicks in for anything that didn't go through a
slash-command summary, including ad-hoc fixes that touched >2 files
or any PIPELINE_RULES / RUNBOOK / CLAUDE.md edit.

**Enforcement.** Manual; reviewer convention. Future tooling could
hook `git commit` to require a session marker, not blocking now.

---

## Rule 12 — Canonical Curation Marker

**New rule, learned the hard way.**

After Phase 3 of the dristi-common pipeline picks a "majority variant"
canonical, a reviewer often refactors it (merging features from the
secondary cluster, extracting DTOs, fixing imports). Re-running Phase 3
because the inventory changed (e.g. after Phase 4a deletes shifted the
majority) will silently overwrite that work.

**Rule.** The first non-blank line of any hand-edited canonical or
hand-edited target-module file must contain the exact string
`// HAND-CURATED`. Phase 3 of `dristi_common/03_build_canonical.py`
and Phases 3 + 6 of the per-module pipeline both check for this marker
and refuse to overwrite.

---

## Rule 31 — API-First Cross-Subdomain Boundary

**New rule, learned from the LockApi/CaseApi cutover PR.**

`domain-case-lifecycle` is one Maven module with multiple subdomains
(cases, locksvc, order, notification). Spring Modulith's default
detection treats the Maven module as the unit; subdomains are sub-
packages of the same module and freely cross-import. That defeats the
DDD bounded-context separation each subdomain should preserve — they
were independently authored microservices before the monolith pulled
them in.

**Rule.** Each subdomain marks itself as a Spring Modulith application
module via `package-info.java`:

```java
// <subdomain>/package-info.java
@org.springframework.modulith.ApplicationModule(displayName = "Case")
package org.pucar.dristi.<domain>.<subdomain>;
```

After this, cross-subdomain imports of `internal/...` fail
`ModuleStructureTest.verify()`. Other subdomains may consume the
subdomain only via:

1. **Top-level `<Subdomain>Api.java`** at the subdomain root (auto-
   exposed because top-level types are public-by-default per Modulith
   convention). One `*Api` per subdomain by default; split when surface
   grows past ~10 methods or naturally bifurcates.
2. **Contract DTOs**: either lifted to `dristi-common/contract/<subdomain>/`
   (Rule 24) or exposed in-place via `@NamedInterface("contract")`
   (Rule 24a). `*Api` signatures use these types.

**Implementation lives in `<subdomain>/internal/service/`** and
delegates to the existing internal services. Keep impls thin —
business logic stays in the existing service classes.

**When to expose `*Api` in a subdomain:** add it on first cross-subdomain
need OR forward-looking when the registry shows imminent demand. The
order subdomain's `OrderApi.search` was added speculatively because
hearing/task/evidence/etc. are in flight and will all consume it.

**Enforcement.** `ModuleStructureTest.verify()` (Spring Modulith
`ApplicationModules.of(DristiApplication.class)`) — already in the
build, just runs against the new markers automatically.

---

## Rule 32 — REST→Direct Converts at Target-Migration Time, Not Caller-Migration Time

**New rule, learned from the LockApi/CaseApi cutover PR.**
**Supersedes Rule 27.**

Rule 27 said "REST→direct = separate follow-up PR" because doing the
conversion required behavioural surgery and the API-first
infrastructure (Rules 31, 24a) didn't exist. With that infrastructure
in place, conversion belongs in the **target service's migration PR**
— the same PR that adds the target's `*Api`.

**Rule.** When service `T` migrates, the migration PR carries three
concerns in this order:

1. **C1 (structural lift):** unchanged from Rule 28.
2. **C2 (contract uplift + REST→direct):** Phase 35 (or Rule 24a's
   `@NamedInterface` if Phase 35 isn't feasible) + add `<T>Api` at
   subdomain top-level + rewrite every `done` caller's REST call to
   `T` as a direct `*Api` injection.
3. **C3 (pipeline/rules/docs):** unchanged.

The caller-side migration PR (when caller `C` originally migrated)
**leaves the REST call untouched** — `T` doesn't exist in the monolith
yet, and there's nothing to point at. The deferred conversion lands
in `T`'s PR, not as endless follow-up PRs that never get cleaned up.

**Decision tree** (replaces Rule 27's table):

| Caller's target | Action in target's migration PR (when target = `T`) |
|---|---|
| Migrated DRISTI service | Convert. Caller switches from REST util to `@Autowired <T>Api` |
| Unmigrated DRISTI service | N/A — caller's REST call stays until that target migrates |
| Platform (eGov / DIGIT — see `EGOV_HOST_TOKENS`) | Permanent REST. Never converts |

**Side effect of conversion** (must be done in the same PR):

- Caller's REST helper util gets deleted. Don't keep it as a one-line
  wrapper — see [Rule 38](#rule-38--delete-rest-helper-utils-on-conversion-dont-wrap).
- Caller's per-subdomain YAML loses `<target>.host`/`<target>.path`
  keys; the corresponding `Configuration` getter methods get dropped.
- Caller's tests stop mocking `RestTemplate`/`serviceRequestRepository`
  for that target; they mock the `*Api` instead — see Rule 36.

**Exception.** A first-of-its-kind retro-conversion (cleaning up
done→done calls that pre-existed because Rules 31/32 didn't exist
during their migrations) may go in a dedicated PR scoped to the
cleanup, as the LockApi/CaseApi cutover did. Once retro-cleanup is
done, Rule 32 governs forward.

**Enforcement.** None automated; reviewer convention. A future Phase
5 enhancement could enumerate done→done REST candidates and fail the
build if any remain after target's PR.

---

## Rule 33 — RequestInfo Is an Explicit Parameter on Every `*Api` Method

**New rule, learned from the LockApi/CaseApi cutover PR.**

REST calls passed `RequestInfo` in the body envelope; Spring's request
context made the value implicitly available via thread-locals.
Direct calls bypass that machinery — if `RequestInfo` is implicit, the
caller's identity, audit metadata, and correlation IDs disappear at
the boundary.

**Rule.** Every `*Api` method takes `org.egov.common.contract.request.RequestInfo`
as an explicit parameter. The caller threads its inbound `RequestInfo`
through:

```java
public interface LockApi {
    boolean isLockPresent(RequestInfo requestInfo, String uniqueId, String tenantId);
}
```

Even when the method's logical signature is just `(uniqueId, tenantId)`,
RequestInfo stays. Rationale:

- Audit trail and authorisation checks downstream need the caller
  identity. Implicit context loses this.
- Future `@Async` adoption breaks thread-local context propagation
  silently. Explicit param survives.
- Testability — tests construct `RequestInfo` directly rather than
  setting up Spring context.

**Enforcement.** Code review. The pattern is documented in the three
existing `*Api` interfaces; new APIs should mirror.

---

## Rule 34 — `*Api` Signatures Use Contract DTOs Only

**New rule, learned from the LockApi/CaseApi cutover PR.**

The whole point of the API boundary is that callers don't see
implementation types. Leaking an internal type into a method signature
gives the caller an excuse to import it transitively, which defeats
the boundary.

**Rule.** Every type in an `*Api` method's parameters or return type
is one of:

1. A **contract DTO** lifted to `dristi-common/contract/<subdomain>/`
   (Rule 24).
2. A **contract DTO** in `<subdomain>/internal/web/models/` with the
   package marked `@NamedInterface("contract")` (Rule 24a).
3. A **platform / eGov type** from `org.egov.common.contract.*` (e.g.
   `RequestInfo`, `ResponseInfo`).
4. A **JDK primitive or standard collection** (`boolean`, `String`,
   `List<X>` where X is itself permissible).

Internal types from `internal/` (other than the Rule 24a contract
package) must not appear. If a useful return value is an internal
type, the impl converts it to a contract type at the boundary.

**Concrete example from this PR.** `OrderApi.search` returns
`OrderListResponse` (lifted to `contract/order/`). Internally,
`OrderRegistrationService.searchOrder` returns `List<Order>`. The
`OrderApiImpl` wraps:

```java
List<Order> orders = orderRegistrationService.searchOrder(request);
return OrderListResponse.builder()
    .list(orders).totalCount(...).pagination(...).responseInfo(...).build();
```

The wrap is the API's job, not the caller's.

**Enforcement.** Spring Modulith `verify()` flags imports of
non-exposed internal types; that's the technical enforcement. Code
review covers the design intent.

---

## Rule 35 — Cross-Module Writes Are Tier 3

**New rule, learned from the LockApi/CaseApi cutover PR.**

Cross-subdomain *reads* (caller asks target for data) are routine and
covered by `*Api`. Cross-subdomain **writes** (caller asks target to
mutate state) are different — they raise transaction boundaries,
idempotency, rollback, eventual consistency, and audit-trail
questions that the read path doesn't.

**Rule.** Default `*Api` shape is read-only / idempotent. Adding a
*write* method to a `*Api` is **Tier 3** (per
[scripts/migration/CLAUDE.md §2](CLAUDE.md)) — propose 2-3 design
options with trade-offs, surface to the user, and wait for direction.
Specifically discuss:

1. **Transaction boundary.** Does the caller's `@Transactional`
   propagate through the direct call? If yes, a target-side rollback
   surfaces as a caller-side rollback. If no, partial-write states
   need explicit reconciliation.
2. **Idempotency.** Does the target's write tolerate replay? If the
   caller retries (e.g. on a transient exception), is the target's
   state consistent?
3. **Audit trail.** Cross-module write events are easy to lose. Add
   structured logging at the API boundary (enter/exit + correlation
   ID from `RequestInfo.userInfo`).
4. **Event vs. RPC.** Sometimes the right shape is "publish event,
   target subscribes" rather than "direct call." Spring Modulith
   supports `ApplicationModuleListener` for this.

**Enforcement.** Reviewer discipline. Code review on any `*Api` PR
asks "does this method mutate target state? if so, did we hit Tier 3?"

---

## Rule 36 — Convert Tests at the Same Time as the Call

**New rule, learned from the LockApi/CaseApi cutover PR.**

Caller tests previously mocked `RestTemplate.postForObject(...)` or
`serviceRequestRepository.fetchResult(...)` to stub the REST round-
trip. After conversion, those mocks no longer exercise the real call
path — the call goes through `<T>Api` now.

**Rule.** When a caller's REST call to target `T` becomes a direct
`<T>Api` call, the same commit:

1. Removes the `RestTemplate` / `ServiceRequestRepository` mocks for
   that target.
2. Adds `@Mock <T>Api` to the caller's test class.
3. Stubs the equivalent shape with `when(api.method(...)).thenReturn(...)`
   using contract DTOs (typed, not `Map.class` or `JsonNode`).
4. Updates `verify(...)` calls to match the new method.

**Concrete patterns from this PR**:

```java
// BEFORE (REST stub of fetchCaseDetails returning Boolean)
when(caseUtil.fetchCaseDetails(any(), eq(cnr), eq(filing))).thenReturn(true);
verify(caseUtil).fetchCaseDetails(any(), eq(cnr), eq(filing));

// AFTER (typed *Api stub returning a built CaseExistsResponse)
CaseExists matched = new CaseExists();
matched.setExists(true);
when(caseApi.exists(any())).thenReturn(
    CaseExistsResponse.builder().criteria(Collections.singletonList(matched)).build());
verify(caseApi).exists(any());
```

```java
// BEFORE (REST stub of searchCaseDetails returning JsonNode)
JsonNode mockedDetails = mock(JsonNode.class);
when(mockedDetails.get("courtId")).thenReturn(courtIdNode);
when(caseUtil.searchCaseDetails(any())).thenReturn(mockedDetails);

// AFTER (typed *Api stub returning a built CaseListResponse with one CourtCase)
CourtCase mockCase = new CourtCase();
mockCase.setCourtId("COURT123");
CaseCriteria criterion = new CaseCriteria();
criterion.setResponseList(Collections.singletonList(mockCase));
when(caseApi.search(any())).thenReturn(
    CaseListResponse.builder().criteria(Collections.singletonList(criterion)).build());
```

The new mock pattern is more verbose but typed — refactoring renames
catch errors that JsonNode-mocked tests would miss.

**Enforcement.** Review-time. The conversion's mvn test must pass.

---

## Rule 37 — Dead Code Surfaces During Cutover; Sweep It Out

**New rule, learned from the LockApi/CaseApi cutover PR.**

REST→direct cutovers are exactly the moment to find legacy dead code
the original team meant to clean up. Concrete patterns surfaced
during the LockApi/CaseApi cutover:

1. **Dead injection.** A util is `@Autowired` into a service but no
   method on the service ever calls it. `cases/internal/service/CaseService`
   declared `OrderUtil orderUtil` and assigned it — never invoked.
2. **Orphaned local DTOs.** Types in `<subdomain>/internal/web/models/`
   that only the now-deleted util referenced. `cases` had local
   `OrderListResponse` and `OrderSearchRequest` only `OrderUtil` saw.
3. **Unused helper methods.** A util has 6 methods, 2 are REST callers,
   the other 4 are pure JSON helpers nobody calls. `order/CaseUtil`
   shipped 4 helpers (`getLitigants`, `getIndividualIds`,
   `getRepresentatives`, `getAdvocateIds`) used nowhere.
4. **Contract orphans.** When two services lifted their own view of a
   shared DTO into separate `contract/<subdomain>/*` namespaces, one
   becomes the canonical and the other an orphan. Order's
   `contract/order/{CaseExists*, CaseSearchRequest, CaseCriteria}`
   were orphans against case's `internal/web/models` types.
5. **Configuration drift.** When the REST helper util goes, its
   `<svc>.host`/`<svc>.path` `@Value`-bound fields go too — and the
   per-subdomain YAML keys that fed them.

**Rule.** During a REST→direct cutover, in the same commit:

- **Grep first**: `grep -rE "<utilName>\\." <subdomain>/src/main/java`
  to find call sites. If empty, the util is dead — delete entirely.
- **Walk the imports of the deleted util.** Any local DTO whose only
  importer is the deleted util becomes an orphan — delete it too.
- **Check the util's other methods** for usage. Methods nobody calls
  are dead even if the rest of the util isn't.
- **Find duplicates.** Run `comm -12 <(ls contract/<a>/) <(ls contract/<b>/)`
  for any pair of post-Phase-35 contract dirs — overlapping names are
  candidates for dedupe.
- **Drop now-unused config.** Each `Configuration` `@Value` field that
  fed only the deleted util gets removed; corresponding YAML keys
  too.

These cleanups belong in the **same** commit as the conversion, not a
follow-up. Splitting hides the connection.

**Enforcement.** None automated. Surfaces during code review of the
cutover commit.

---

## Rule 38 — Delete REST Helper Utils on Conversion, Don't Wrap

**New rule, learned from the LockApi/CaseApi cutover PR.**

Tempting to keep the REST util as a thin pass-through after conversion:

```java
// Don't do this.
@Component
public class LockUtil {
    @Autowired private LockApi lockApi;
    public boolean isLockPresent(RequestInfo r, String u, String t) {
        return lockApi.isLockPresent(r, u, t);
    }
}
```

It's smaller blast radius (callers don't change the injected type) but
it's a half-finished refactor — the wrapper does literally nothing
and accumulates as technical debt that nobody schedules to remove.

**Rule.** When a REST util is converted to `*Api`, **delete the util**.
Caller call sites switch from `@Autowired <X>Util util;` to
`@Autowired <X>Api api;`. Tests update at the same time (Rule 36).

**Exception.** If a util has both REST methods AND non-trivial
non-REST behaviour (formatting, validation, multi-call orchestration),
split: extract the REST methods into the `*Api` callers; keep the
non-REST behaviour in a renamed helper class (`<X>Helper` or
`<X>JsonHelper`). The original util class goes away.

**Cited cleanup from this PR**: `order/CaseUtil` had 2 REST methods
and 4 pure JSON helpers. Of the 4 helpers, all 4 turned out to be
dead code (Rule 37) — so the entire util went away with no helper
class needed. If the helpers had been live, they'd have moved to
`order/internal/util/CaseJsonHelper`.

**Enforcement.** Code review. If reviewer sees a one-line
`*Api`-wrapping util after a conversion, push back.

---

## Useful checks at a glance

| What                                  | Where                                             |
|---------------------------------------|---------------------------------------------------|
| Run the full per-module pipeline      | `scripts/migration/per_module/run_module_migration.py --service X --module Y --subdomain Z` |
| Run a single phase                    | `--phase 3` or `--phase 1,2,3`                    |
| Re-extract dristi-common canonicals   | `scripts/migration/dristi_common/run_phases_1_to_3.sh` |
| Safe replace (hash-match deletes only)| `scripts/migration/dristi_common/04a_replace_safe.py [--dry-run]` |
| Validate dristi-common after Phase 4a | `scripts/migration/dristi_common/05_validate.py`  |
| Regenerate scaffold                    | `scripts/migration/scaffold/run_pipeline.sh`     |
