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
```

Pre-flight rules:
- **Tree must be clean.** Any uncommitted changes → stop, ask user.
- **Branch should be `monolith/<service>`.** If on `monolith/main` or
  another branch, ask user whether to create the per-service branch
  now (`git checkout -b monolith/<service>`).
- **Source dir must exist** under `dristi-services/` or
  `integration-services/`. If neither, the service name is wrong.

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

- **Flyway `_2` rename** if Gate 7 collides with an existing migration
  version (Rule 24 collision rule).
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

### 3.2 Read REST calls and classify

```bash
cat scripts/migration/per_module/output/<service>_rest_calls.txt 2>/dev/null
cat scripts/migration/per_module/output/<service>_contract_lift.txt 2>/dev/null
```

For each REST call:
- **Tier 1 to convert:** target service is already in the monolith
  (check SERVICE_REGISTRY for `done` rows) AND the call shape is
  straightforward (typed DTO in/out). Do the conversion in the migrated
  tree, show the diff.
- **Tier 4 (ask user):** target service is unmigrated, or the call uses
  `Object`-typed payloads, or the host getter name is ambiguous. List
  these and ask which to convert.
- **Skip:** target is a platform service (eGov / DIGIT). Rule 17 lists
  the tokens. Confirm in chat which were skipped and why.

If lifted DTOs need legacy deps in `dristi-common` (e.g.
`digit-models`, `swagger-core:1.5.18`), add them to
`dristi-monolith/dristi-common/pom.xml` per Rule 24's note.

### 3.3 Build verification

```bash
cd dristi-monolith && \
  JAVA_HOME=/home/mani/.jdks/corretto-17.0.18 \
  PATH=/home/mani/.jdks/corretto-17.0.18/bin:$PATH \
  mvn -B -pl domain-<module> -am test \
  -Dsurefire.failIfNoSpecifiedTests=false \
  2>&1 | tail -200 > /tmp/mvn-test-<service>-c2.log
echo "exit=$?"

mvn -B -pl dristi-app -am package 2>&1 | tail -100 > /tmp/mvn-package-<service>-c2.log
echo "exit=$?"
```

### 3.4 C2 summary and pause

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
- `mvn` failure at Step 2.5 or 3.3 → diagnose, do not commit.
- User says "stop" or shows hesitation → stop immediately.
- **Never collapse C1 + C2 into one commit** — Rule 28 violation.
