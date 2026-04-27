# Agentic Migration Skills (refined)

This is the working skill set used by the per-module migration pipeline
(`scripts/migration/per_module/run_module_migration.py`) and the
`dristi-common` extraction pipeline (`scripts/migration/dristi_common/`).

It supersedes the original 10-skill list in `agentic_workflow_april_27.md`
where reality required adjustments. Every "Refined by" note links the rule
to the concrete experience that produced it.

---

## Skill 1 — Package Naming Enforcer

**Rule.** Every Java file under `dristi-monolith/domain-<module>/` declares
`org.pucar.dristi.<module>.<subdomain>.internal.*`. Public API types live
one level up at `org.pucar.dristi.<module>.<subdomain>.*Api`.

**Enforcement.** `phase_7_validate` Gate 4 walks every file in the target
tree and fails if its `package` line doesn't start with the expected
`<target>.internal` prefix.

---

## Skill 2 — Duplicate Elimination Guard

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

## Skill 3 — Module Boundary Enforcer (Spring Modulith)

**Rule.** Code in `domain-X` may not import from `domain-Y.*.internal.*`.
Cross-module communication goes through `*Api` interfaces.

**Enforcement.** `dristi-app/src/test/java/.../ModuleStructureTest` calls
`ApplicationModules.of(DristiApplication.class).verify()`.

**Refined by reality.** Spring Modulith 1.1.6 is the version compatible
with Spring Boot 3.2.x — pinned in `dristi-app/pom.xml` not the parent
POM (the parent stays plain so child modules can opt out of modulith if
they're libraries-only).

---

## Skill 4 — REST-to-Method-Call Converter

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

## Skill 5 — Kafka Topic Hygiene

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

## Skill 6 — Persistence Pattern Guard

**Rule.** Phase 1 — all writes go via `Producer → topic →
egov-persister`. Phase 2 — migrated modules use `@Repository` +
`@Transactional`. No mixing.

(Unchanged from the original doc; not exercised yet.)

---

## Skill 7 — Configuration Consolidation Guard

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

## Skill 8 — Import Sanitizer

**Rule.** Inside `dristi-monolith/domain-*/`, banned import prefixes are:
`digit.`, `pucar.`, `notification.`, `drishti.payment.`,
`com.egov.icops`, `com.dristi.njdg`, `org.egov.eTreasury.`,
`org.egov.transformer.`, `org.drishti.esign.`, `com.pucar.drishti.`.

**Enforcement.** `phase_7_validate` Gate 1.

---

## Skill 9 — Automated Package Rename Engine

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

## Skill 10 — Schema Isolation Guard (Phase 2)

**Rule.** Each module's `@Entity`/`@Table` declares an explicit
`schema=` matching its domain (`case_lifecycle`, `identity`,
`integration`, `financial`).

(Not exercised yet; deferred to Phase 2 of the migration.)

---

## Skill 11 — Test Migration & Verification Guard

**Rule.** `src/test/java` for the migrated service is copied to the
target module's `src/test/java/.../internal/` with the same package
rewrite applied. Tests that mock `ServiceRequestRepository` for an
intra-DRISTI call need their mock retargeted to the direct service
call.

**Refined by reality.** The HAND-CURATED guard applies to test files
too — once a reviewer has fixed up a test's mocks, re-running the
pipeline will not undo that work.

---

## Skill 13 — Service-Augmented Helper Detection

**New skill, learned from the lock-svc test drive.**

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

## Skill 12 — Canonical Curation Marker

**New skill, learned the hard way.**

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
