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
  PR is owned separately.

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

**New rule, learned from the order service migration.**

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

1. Branch from `monolith/main`.
2. Run pipeline with `--phase 1,2,3,4,5,6,7,8,9` (excluding 35).
   Apply manual fixes. Verify `mvn test -pl domain-<module>`.
   **Commit C1.**
3. Run `--phase 35`. Add any required `dristi-common` deps. Convert
   straightforward REST callers per Rule 27. Verify build.
   **Commit C2.**
4. If this migration motivated pipeline / rule changes, edit them now
   and **commit C3**. Otherwise, skip.

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

## Useful checks at a glance

| What                                  | Where                                             |
|---------------------------------------|---------------------------------------------------|
| Run the full per-module pipeline      | `scripts/migration/per_module/run_module_migration.py --service X --module Y --subdomain Z` |
| Run a single phase                    | `--phase 3` or `--phase 1,2,3`                    |
| Re-extract dristi-common canonicals   | `scripts/migration/dristi_common/run_phases_1_to_3.sh` |
| Safe replace (hash-match deletes only)| `scripts/migration/dristi_common/04a_replace_safe.py [--dry-run]` |
| Validate dristi-common after Phase 4a | `scripts/migration/dristi_common/05_validate.py`  |
| Regenerate scaffold                    | `scripts/migration/scaffold/run_pipeline.sh`     |
