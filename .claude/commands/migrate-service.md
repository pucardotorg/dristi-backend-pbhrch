---
description: Run the per-module migration pipeline end-to-end with Rule 28 C1/C2 commit boundaries, config consolidation, and build verification.
argument-hint: <service> <module> <subdomain>
---

# /migrate-service

Migrate one DRISTI service into the modular monolith following the
**Rule 28 three-commit structure** (see
[scripts/migration/PIPELINE_RULES.md](scripts/migration/PIPELINE_RULES.md)
Rule 28 and [scripts/migration/RUNBOOK.md](scripts/migration/RUNBOOK.md)
§8):

- **C1 — structural lift:** pipeline phases 1-9 (no Phase 35), config
  consolidation, manual Tier 1 fixes, build verify.
- **C2 — contract uplift + REST→direct:** Phase 35 + REST conversions,
  build verify.
- **C3 — pipeline/rules/docs:** optional, only if this migration
  motivated changes to pipeline source.

Each commit is a clean diff boundary. **Do not collapse C1 + C2 into a
single bundled commit** — `order` did this and explicitly noted it
as a one-time exception ("hearing onwards uses the 3-commit structure
per Rule 28"). Bundling defeats bisect-ability between "did we move
files" and "did the contract lift change behaviour".

**Arguments:** `$ARGUMENTS` — expected order: `<service> <module> <subdomain>`

If arguments are missing or malformed, stop and ask the user. Do not
guess defaults from the SERVICE_REGISTRY.

---

## Step 0 — Parse and confirm

Parse the three arguments. Show them back to the user as a single
confirmation line:

> "About to migrate `<service>` → `domain-<module>/<subdomain>`.
> Branch should be `monolith/<service>`. Confirm?"

Wait for explicit yes before proceeding to Step 1.

If the user passed only one or two args, ask for the missing ones —
point at `scripts/migration/SERVICE_REGISTRY.md` for the canonical
`(module, subdomain)` mapping.

---

## Step 1 — Pre-flight

```bash
git status --short                                    # tree must be clean
git rev-parse --abbrev-ref HEAD                       # branch name
ls dristi-services/<service> 2>/dev/null \
  || ls integration-services/<service> 2>/dev/null    # source must exist
git fetch origin
git merge-base --is-ancestor origin/monolith/main HEAD \
  && echo "branch is up to date" \
  || echo "BEHIND monolith/main — rebase first"
```

Pre-flight rules:
- **Tree must be clean.** Any uncommitted changes → stop, ask user.
- **Branch should be `monolith/<service>`.** If on `monolith/main` or
  another branch, ask user whether to create the per-service branch
  now (`git checkout -b monolith/<service>`).
- **Source dir must exist** under `dristi-services/` or
  `integration-services/`. If neither, the service name is wrong.
- **Branch must be up to date with `monolith/main`.** If `BEHIND`,
  surface to user: *"`monolith/main` has moved since this branch
  diverged. Merge it in (`git merge origin/monolith/main`), resolve
  conflicts (typically just `application.yml` profile list), re-run
  consolidation with cumulative `--service` list per Step 2.3, and
  re-run Step 1 before proceeding."* Wait for confirmation. See
  [PARALLEL_MIGRATION_PLAN.md §5](../../scripts/migration/PARALLEL_MIGRATION_PLAN.md)
  for the manual rebase recipe.

---

## Step 2 — C1: Structural lift

### 2.1 Run pipeline phases 1-9 (no Phase 35)

```bash
python3 scripts/migration/per_module/run_module_migration.py \
  --service <service> --module <module> --subdomain <subdomain> \
  --phase 1,2,3,4,5,6,7,8,9 \
  2>&1 | tee /tmp/migration-<service>-c1.log
```

If the run takes >2 min, use `run_in_background: true` and monitor.

Parse the gate summary:

```bash
grep -E "^(PASS|FAIL) Gate" /tmp/migration-<service>-c1.log
```

**If any gate fails:** invoke the `/debug-gate` workflow for that gate.
Do not proceed past Step 2.1 with a failing gate.

### 2.2 Read manual-review files — followups only at C1

```bash
cat scripts/migration/per_module/output/<service>_followups.txt 2>/dev/null
```

For each non-empty entry, this is a **Tier 3 decision per file** —
lift extra methods into the canonical in `dristi-common` vs. keep as
service-local helper. Lay out the tradeoff per file (consult Rules 13,
15, 18, 19), wait for user decision before any pipeline-level edit.

**Skip `_rest_calls.txt` here.** REST→direct conversions belong to C2,
not C1. Phase 35 has not run yet, so contracts aren't lifted yet.

### 2.3 Config consolidation

Determine the full list of services already in the monolith plus the
new one. Read `scripts/migration/SERVICE_REGISTRY.md` and extract every
service whose status is `done`; do NOT hardcode the list (it rots).

```bash
python3 scripts/migration/config_consolidation/run_consolidation.py \
  --service <each done service> --service <service>
```

Then edit `dristi-app/src/main/resources/application.yml` to add the
new subdomain to `spring.profiles.active`. Order matters — later
profiles override earlier ones:

```yaml
spring:
  profiles:
    active: shared,<existing subdomains in order>,<new subdomain>,local
```

Read the conflict report:

```bash
cat scripts/migration/config_consolidation/output/config_conflicts.txt 2>/dev/null
```

Filter for entries that mention the new service. Spring's profile-overlay
order resolves them; flag any that look behaviorally significant.

### 2.4 Known Tier 1 manual fixes

Apply these if the service has them (each has bitten every prior
migration):

- **Flyway suffix / normalization** — consult
  [scripts/migration/flyway-suffix-allocation.md](../../scripts/migration/flyway-suffix-allocation.md)
  for this service's pre-allocated action. Apply Tier 1 actions
  deterministically (collision suffix, zero-pad). For Tier 4 entries
  (e.g. `casemanagement`, `openapi` ambiguous-year normalization),
  surface the candidate target filename and ask the dev to confirm
  via `git log` of the source SQL file.
- **Canonical signature adapt** per Rule 26 — caller code that depends
  on a service-local signature of a now-canonical class needs editing
  (e.g. `MdmsUtil.fetchMdmsData` `Map<...>` return).
- **Parent pom dep bump** per Rule 25 — e.g. `mockito-core: 3.12.4 →
  5.7.0` if tests fail with `WrongTypeOfReturnValue` on Jackson types.
- **Controller test rewrite from `@WebMvcTest` to `@MockitoExtension`.**
  Every controller test moved to a domain module fails with
  `Unable to find a @SpringBootConfiguration` because there's no
  `@SpringBootApplication` in the domain module (the only one lives
  in `dristi-app`, which domain modules don't depend on). Rewrite
  the test to use `@ExtendWith(MockitoExtension.class)` +
  `@InjectMocks` controller + `@Mock` collaborators, calling the
  controller method directly and asserting on `ResponseEntity`. Drop
  any test cases that exercised Spring MVC plumbing
  (e.g. `@Valid` 400-on-missing-body) — those test framework code,
  not your code. Pattern: see prior migrations'
  `LockApiControllerTest` / `OrderApiControllerTest`.

### 2.5 Build verification

```bash
cd dristi-monolith && \
  JAVA_HOME=/home/mani/.jdks/corretto-17.0.18 \
  PATH=/home/mani/.jdks/corretto-17.0.18/bin:$PATH \
  mvn -B -pl domain-<module> -am test \
  -Dsurefire.failIfNoSpecifiedTests=false \
  2>&1 | tail -200 > /tmp/mvn-test-<service>-c1.log
echo "exit=$?"
```

If exit != 0, grep for `[ERROR]` lines first. Only read full files
when investigating a specific failure. Failures here are real — do
not handwave.

If tests pass, build the fat JAR:

```bash
mvn -B -pl dristi-app -am package 2>&1 | tail -100 > /tmp/mvn-package-<service>-c1.log
echo "exit=$?"
```

### 2.6 C1 summary and pause

Print the C1 summary (files migrated, deduped classes, Tier 1 fixes,
config conflicts, gate status, build status) and **stop**.

Suggest the C1 commit message per RUNBOOK §8 C1:

```
migrate(<service>): structural lift to domain-<module>/<subdomain>

- N main + M test files moved from <source>
- K protected dups deduped to dristi-common
- Per-controller @RequestMapping prefix /<context-path>
- Config: shared/<existing>/<subdomain>/local profiles wired
- Manual Tier 1 fixes: <list>
```

**Wait for explicit "commit C1" / "ship C1" before staging.** On
confirmation, stage exactly:

```bash
git add dristi-monolith/                                                # domain code + per-subdomain yml
git add scripts/migration/per_module/output/<service>_manifest.json     # migration record
git add scripts/migration/per_module/output/<service>_followups.txt     # iff non-empty (Tier 3 decisions)
git add scripts/migration/config_consolidation/output/                  # config_conflicts.txt + report.csv
```

These outputs are the audit trail for what the pipeline did at C1; prior
migrations missed them and required a fixup commit. Do not use
`git add -A` — it sweeps in unrelated `.claude/` settings and stale
build artefacts.

---

## Step 3 — C2: Contract uplift + REST→direct

### 3.1 Run Phase 35

```bash
python3 scripts/migration/per_module/run_module_migration.py \
  --service <service> --module <module> --subdomain <subdomain> \
  --phase 35 \
  2>&1 | tee /tmp/migration-<service>-c2.log
```

Verify Gate 8 still passes (no contract-suffixed classes left in
`internal/web/models/`):

```bash
grep -E "^(PASS|FAIL) Gate" /tmp/migration-<service>-c2.log
```

### 3.2 Declare `<Subdomain>Api` + `@ApplicationModule` (Rules 31, 33, 34)

If any subdomain (already `done` or in-flight on a peer branch) calls
into this service via REST, expose those methods on a top-level
`<Subdomain>Api` so callers can switch from REST → direct in this PR
(Rule 32 — at target-migration time).

**Identify required methods.** Survey both sources:

```bash
# Grep `done` services' rest_calls for calls targeting <service>
grep -l "<host-token-for-this-service>" \
  scripts/migration/per_module/output/*_rest_calls.txt

# Coordinator should also share peer branches' rest_calls files
# (in-flight services may need methods now to avoid cross-PR coordination).
```

For each unique method, draft the `<Subdomain>Api` signature:
- **Rule 33:** `RequestInfo` is an explicit first parameter — never thread-local.
- **Rule 34:** parameter and return types are contract DTOs only —
  Phase-35-lifted to `dristi-common/contract/<subdomain>/` (Rule 24)
  or in-place via `@NamedInterface("contract")` (Rule 24a).
- **Rule 35:** if the method is a *write* (mutates target state), STOP
  — that's Tier 3, surface 2-3 design options to the dev.

**Surface to dev before scaffolding:**

> "Identified N REST calls into `<service>` from `done`/in-flight services
> [list]. Proposed `<Subdomain>Api` shape:
>   - `boolean methodA(RequestInfo, ...)`
>   - `<DTO> methodB(RequestInfo, ...)`
> Confirm before creating the interface + impl + ApplicationModule marker?"

Wait for explicit OK. Then create three files (templates: `LockApi`,
`OrderApi`, `CaseApi` from commit `7790b59c2`):

- `<subdomain>/<Subdomain>Api.java` — top-level interface
- `<subdomain>/internal/service/<Subdomain>ApiImpl.java` — thin delegate
  to existing internal service classes; no new business logic
- `<subdomain>/package-info.java` — `@ApplicationModule(displayName = "<Subdomain>")`

If a needed method requires a Tier 3/4 decision Claude can't resolve
(e.g. an in-flight caller wants a method that touches Rule 35
territory), stop. Do not infer.

If no other subdomain calls into this service (rare for case-lifecycle,
normal for leaf integration services like `treasury-backend`), skip
`<Subdomain>Api` entirely. Still add `@ApplicationModule` for boundary
enforcement.

### 3.3 Convert REST → direct (both directions)

#### (a) `done` callers calling INTO `<service>` (Rule 32)

For each `done` service identified in 3.2 that called into `<service>`
via REST, edit its caller code in this PR:

```java
// BEFORE (in done service)
@Autowired private <Service>Util util;
... util.method(request)

// AFTER
@Autowired private <Subdomain>Api api;
... api.method(requestInfo, ...)
```

- **Rule 38:** delete the REST helper util in the caller — don't keep
  it as a one-line wrapper.
- **Rule 36:** update caller's tests in the same diff — mock
  `<Subdomain>Api`, not `RestTemplate`/`serviceRequestRepository`.
- **Rule 37:** sweep dead code surfaced by the conversion (orphaned
  DTOs, unused config keys, dead `@Autowired` fields).

Show each caller diff before applying. Wait for OK on each.

#### (b) `<service>`'s own REST calls TO `done` services

Read `<service>_rest_calls.txt`:

```bash
cat scripts/migration/per_module/output/<service>_rest_calls.txt 2>/dev/null
cat scripts/migration/per_module/output/<service>_contract_lift.txt 2>/dev/null
```

For each REST call:
- **Tier 1 to convert:** target service is already in the monolith
  (check SERVICE_REGISTRY for `done` rows) AND the call shape is
  straightforward (typed DTO in/out) AND the target's `*Api` exposes
  the needed method. Switch to `@Autowired <Target>Api`. Show the diff.
  If the target's `*Api` doesn't expose the needed method, **stop and
  escalate per Rule 39 (Tier 4)** — dev decides whether to coordinate
  cross-PR exposure or leave as REST temporarily.
- **Tier 4 (ask user):** target service is unmigrated, or the call uses
  `Object`-typed payloads, or the host getter name is ambiguous. List
  these and ask which to convert.
- **Skip:** target is a platform service (eGov / DIGIT). Rule 17 lists
  the tokens. Confirm in chat which were skipped and why.

If lifted DTOs need legacy deps in `dristi-common` (e.g.
`digit-models`, `swagger-core:1.5.18`), add them to
`dristi-monolith/dristi-common/pom.xml` per Rule 24's note.

### 3.4 Build verification

#### 3.4.1 Rule 40 mutation scan

Before maven, scan for `RequestInfo` mutations introduced or exposed
by REST→direct conversion. The pattern was dormant under REST
(serialization severed the reference) but leaks across direct calls.

```bash
grep -rnE "(reqInfo|requestInfo|info)\.setUserInfo\(|requestInfo\.getUserInfo\(\)\.(set[A-Z]|getRoles\(\)\.(add|remove)\()" \
  --include="*.java" \
  dristi-monolith/domain-<module>/src/main/java/
```

For each hit, read the enclosing method and classify:
- **Safe (Rule 40 exception):** the mutated `RequestInfo` was declared
  via `RequestInfo X = new RequestInfo()` in the same scope. No leak. Skip.
- **Tier 4 (must fix):** the `RequestInfo` originated from a parameter
  or `*.getRequestInfo()` call. Surface to dev with the fix recipe:
  ```java
  // BEFORE
  requestInfo.getUserInfo().getRoles().add(role);
  downstream.setRequestInfo(requestInfo);

  // AFTER
  downstream.setRequestInfo(
      RequestInfoUtil.withExtraRole(requestInfo, role));
  ```
  Wait for explicit OK per site. Apply only on confirmation.

If any unresolved Tier 4 finds remain, do NOT proceed to 3.4.2 — maven
won't fail on shared-reference mutations; the bug would slip past CI.

#### 3.4.2 Maven verification

```bash
cd dristi-monolith && \
  JAVA_HOME=/home/mani/.jdks/corretto-17.0.18 \
  PATH=/home/mani/.jdks/corretto-17.0.18/bin:$PATH \
  mvn -B -pl domain-<module> -am test \
  -Dsurefire.failIfNoSpecifiedTests=false \
  2>&1 | tail -200 > /tmp/mvn-test-<service>-c2.log
echo "exit=$?"

# Run dristi-app tests explicitly so ModuleStructureTest runs and
# catches Rule 31 boundary violations *before* the package phase.
mvn -B -pl dristi-app -am test \
  -Dsurefire.failIfNoSpecifiedTests=false \
  2>&1 | tail -200 > /tmp/mvn-app-test-<service>-c2.log
grep "ModuleStructureTest\|structural violation" /tmp/mvn-app-test-<service>-c2.log || true
echo "exit=$?"

mvn -B -pl dristi-app -am package 2>&1 | tail -100 > /tmp/mvn-package-<service>-c2.log
echo "exit=$?"
```

### 3.5 C2 summary and pause

Print the C2 summary (contracts lifted, REST calls converted /
deferred / skipped, dristi-common deps added, build status) and
**stop**.

Suggest the C2 commit message per RUNBOOK §8 C2:

```
refactor(<service>): contract uplift + REST→direct calls

- Lifted N contract DTOs to dristi-common/contract/<subdomain>/
- Converted M REST calls to direct method calls
  (targets: <list of migrated services>)
- dristi-common/pom.xml deps: <list, if any>
```

**Wait for explicit "commit C2" / "ship C2" before staging.** On
confirmation, stage exactly:

```bash
git add dristi-monolith/                                                # contract DTOs + caller import rewrites
git add scripts/migration/per_module/output/<service>_contract_lift.txt # Phase 35 audit
git add scripts/migration/per_module/output/<service>_rest_calls.txt    # Phase 5 audit (even if empty)
```

---

## Step 4 — C3 (optional): pipeline / rules / docs

Only if this migration motivated edits to
`scripts/migration/**`, `.claude/commands/migrate-service.md`,
PIPELINE_RULES, or RUNBOOK. Otherwise skip.

```
feat(pipeline): <one-line description>

- <list of changes>
```

---

## Step 5 — Push + PR

When the user says "ship the PR" (or equivalent):

```bash
git push origin monolith/<service>
```

Then `gh pr create --base monolith/main` with the body template from
RUNBOOK §8 ("Push + PR" subsection): Summary / Commits / Follow-ups /
Test plan.

---

## Hard stops during this command

- Failing gate at Step 2.1 or 3.1 → invoke `/debug-gate`, return when fixed.
- Tier 3/4 decision needed → present, wait, do not act.
- `mvn` failure at Step 2.5 or 3.4.2 → diagnose, do not commit.
- **`ModuleStructureTest` violation** at Step 3.4.2 → surface the violating
  file + import line. Boundary breach is fixed by switching to the
  target's `*Api` (Rule 31), not by silencing the test.
- **Rule 40 mutation found** at Step 3.4.1 → STOP. Convert to
  `RequestInfoUtil.withExtraRole/withUser` (defensive copy). Maven won't
  catch shared-reference mutations.
- **Cross-`*Api` method gap** at Step 3.2 or 3.3(b) → STOP. Surface
  options per Rule 39: (a) coordinate with target's migrator to expose
  the method, (b) leave as REST temporarily and file a follow-up. Do
  not edit another subdomain's `*Api` from this branch.
- User says "stop" or shows hesitation → stop immediately.
- **Never collapse C1 + C2 into one commit** — Rule 28 violation.
