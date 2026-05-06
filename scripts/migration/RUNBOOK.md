# Per-Module Migration Runbook

**Audience:** developer migrating one DRISTI service into the modular monolith.
**Time per service:** 30–90 minutes (most of which is automated).
**Output:** one PR that moves a service from `dristi-services/<svc>` into
`dristi-monolith/domain-<module>/.../<subdomain>/internal/`, deduplicates
its protected utilities into `dristi-common`, applies its config + URL
prefix, and ships green tests.

---

## 0. One-time setup (per developer)

| Tool | Version | How |
|---|---|---|
| **JDK 17 (with `javac`)** | 17.x | Corretto-17 recommended. The pipeline auto-detects `~/.jdks/corretto-17.*` if `javac` isn't on `PATH`. |
| **Maven** | 3.8+ | `which mvn` |
| **Python** | 3.10+ | The pipeline uses stdlib + PyYAML (`pip install pyyaml`). |
| **Docker** | running | Postgres + WireMock for local-test. |
| **Git** | 2.x | `git --version` |

Once-only:

```bash
# Clone if you haven't
git clone <repo-url> && cd dristi-backend-pbhrch

# Confirm the long-lived migration branch exists locally
git fetch origin
git checkout monolith/main
git pull
```

---

## 1. Pick the service to migrate

Refer to **[SERVICE_REGISTRY.md](SERVICE_REGISTRY.md)** for:
- Available services + their target `(module, subdomain)`.
- Priority order (dependencies between services).
- Owner / sprint, where assigned.

**Already migrated** (don't re-run unless you mean to):
- `lock-svc`, `case`, `order`, `bank-details`.

---

## 2. Create the per-service feature branch

```bash
git checkout monolith/main
git pull
git checkout -b monolith/<service>
```

Convention: branch name is exactly `monolith/<service>` (use the
directory name from `dristi-services/` or `integration-services/`).

---

## 3. Run the pipeline

Single command per service — the pipeline runs all 10 phases (analyze →
scaffold → auto-rename → contract-lift → tests → deduplicate → REST
detection → deps → db migrations → validate). Look up the `(module,
subdomain)` for your service in `SERVICE_REGISTRY.md`.

Phase IDs are unique handles, not sequence positions: contract-lift is
`35` (alias for the conceptual "Phase 3.5") because argparse parses
ints. See [PIPELINE_RULES.md](PIPELINE_RULES.md) Rule 24.

```bash
python3 scripts/migration/per_module/run_module_migration.py \
  --service <service-name> \
  --module <case-lifecycle | identity-access | integration | payments> \
  --subdomain <chosen-subdomain>
```

**Example for hearing:**

```bash
python3 scripts/migration/per_module/run_module_migration.py \
  --service hearing \
  --module case-lifecycle \
  --subdomain hearing
```

The pipeline prints a summary like:

```
Phase 1 (analyze): wrote scripts/migration/per_module/output/hearing_manifest.json
  main files=196, test files=26
  current package: org.pucar.dristi
  target package:  org.pucar.dristi.caselifecycle.hearing
  source context-path: /hearing
Phase 2 (scaffold): created .../hearing/internal
Phase 3 (auto-rename): copied 195 files; prefixed 1 controller(s) with /hearing
Phase 35 (contract-lift): lifted N class(es) to dristi-monolith/dristi-common/.../contract/hearing; rewrote imports in N file(s); see hearing_contract_lift.txt
Phase 6 (test migration): copied 25 test files
Phase 4 (deduplicate): deleted=N protected-class copies, rewrote=N imports, ...
Phase 5 (REST detection): N intra-DRISTI candidate(s); see ..._rest_calls.txt
Phase 9 (db migrations): copied N SQL file(s) into dristi-monolith/domain-.../<subdomain>/db/migration; registered Flyway location in dristi-app/application.yml
Phase 8: lifted N deps into dristi-monolith/domain-...pom.xml
PASS Gate 1: no banned-package imports
PASS Gate 2: no unresolved protected-class duplicates
PASS Gate 3: no forbidden application config (N per-subdomain YAML(s) allowed)
PASS Gate 4: all target files in expected package
PASS Gate 5: dristi-common compiles
PASS Gate 6: N SQL file(s) under dristi-monolith/domain-.../<subdomain>/db/migration; Flyway location registered
PASS Gate 7: N migration(s) across M version(s); no collisions
PASS Gate 8: no contract-suffixed classes in internal/web/models/
```

All eight gates must `PASS`. If any **FAIL**, see [Troubleshooting](#7-troubleshooting).

---

## 4. Run config consolidation

Pipeline 5 merges every migrated service's `application.properties`
into per-subdomain YAML files.

**Important:** include **every** service that's already in the monolith
(otherwise their per-subdomain YAMLs are deleted/recreated incorrectly).
Currently that's `case`, `lock-svc`, plus your new one:

```bash
python3 scripts/migration/config_consolidation/run_consolidation.py \
  --service case \
  --service lock-svc \
  --service <your-new-service>
```

Then add your service's profile name to `application.yml`:

```yaml
spring:
  profiles:
    active: shared,cases,locksvc,<your-subdomain>,local
```

The profile name is whatever appears as `application-<NAME>.yml` —
exactly the `--subdomain` you passed to the pipeline.

**Review the conflict report** (if any):

```bash
cat scripts/migration/config_consolidation/output/config_conflicts.txt
```

Each conflict means two services have different values for the same
key. Spring's profile-overlay rules (later profiles win) usually
resolve this automatically; flag in the PR description.

---

## 5. Manual review — the parts the pipeline does NOT automate

The pipeline is conservative about anything that could change behavior
without your eyes on it. **You** must:

### 5.1 Convert intra-DRISTI REST calls to direct method calls

```bash
cat scripts/migration/per_module/output/<service>_rest_calls.txt
```

Each entry is a file that uses `serviceRequestRepository` /
`RestTemplate` to hit another DRISTI service. Convert to a direct
`@Autowired` service call:

```java
// BEFORE
StringBuilder uri = new StringBuilder(config.getCaseHost() + config.getCaseSearchPath());
Object result = serviceRequestRepository.fetchResult(uri, request);

// AFTER (when the target is another migrated DRISTI service)
CaseSearchResponse result = caseService.searchCases(request);
```

If the target service hasn't been migrated yet, leave the REST call —
it'll get converted when the target service joins the monolith.

### 5.2 Review follow-up tracker (if non-empty)

```bash
cat scripts/migration/per_module/output/<service>_followups.txt 2>/dev/null
```

Each line is a protected-class file that was kept under follow-up
review (it has methods absent from the canonical). Either:
- Add the missing methods to the canonical (preferred — file an issue
  to lift them in a follow-up commit), or
- Leave it as a service-local helper (already auto-renamed to a
  subdomain-prefixed bean name to avoid Spring conflicts).

### 5.3 Audit workflow util/service if service has either

If the migrating service has `WorkflowUtil` and/or `WorkflowService`,
run the audit per [PIPELINE_RULES.md](PIPELINE_RULES.md) Rule 29:

```bash
diff dristi-services/<svc>/src/main/java/.../util/WorkflowUtil.java \
     dristi-monolith/dristi-common/src/main/java/org/pucar/dristi/common/util/WorkflowUtil.java
```

Things to check:
- **Return value.** Does the source's `updateWorkflowStatus` return
  `state.getState()` or `state.getApplicationStatus()`? Update the
  caller to use the matching canonical method (`updateWorkflowStatus`
  for state-name, `updateWorkflowApplicationStatus` for app-status).
- **Service-local types.** The source likely has its own
  `WorkflowObject` / `ProcessInstanceObject`. They're now shared at
  `dristi-common.models.workflow.*` (in `PROTECTED_CLASSES`). Phase 4
  auto-redirects the imports.
- **WorkflowService.** Stays service-local. Refactor to delegate
  `callWorkFlow` / `getProcessInstanceForWorkflow` /
  `getWorkflowFromProcessInstance` / `getUserListFromUserUuid` to
  the canonical injected `WorkflowUtil`. Keep service-specific
  business logic (businessService picking, payment helpers, role
  checks) local.

### 5.4 Skim the contract-lift report

```bash
cat scripts/migration/per_module/output/<service>_contract_lift.txt
```

Lists every class Phase 35 moved to `dristi-common/contract/<subdomain>/`.
Quick sanity scan — anything that looks service-internal in there (e.g.
a row-mapper context, an internal status enum) is a false positive and
can be hand-curated back. Anything obviously contract-shaped that's
**not** in there is a false negative — file a follow-up to widen the
suffix list or add the class to the lift manually.

### 5.4 Spot-check a controller and a service

Open the migrated controller and verify:
- `@RequestMapping("/<context-path>")` is at class level.
- Endpoint methods unchanged.
- Imports look sane (no leftover `import digit.config.*`).

---

## 6. Verify the build

```bash
# Compile + run unit tests for the affected domain module
JAVA_HOME=/home/mani/.jdks/corretto-17.0.18 \
  PATH=/home/mani/.jdks/corretto-17.0.18/bin:$PATH \
  mvn -B -pl domain-case-lifecycle -am test -Dsurefire.failIfNoSpecifiedTests=false

# Build the fat JAR
mvn -B -pl dristi-app -am package
```

Expected: **BUILD SUCCESS** for both. If you see test failures, they
are real — investigate before committing.

### Local-boot smoke test (optional but recommended)

See [scripts/local-test/README.md](../local-test/README.md) for the
end-to-end recipe: docker Postgres + WireMock + boot the JAR + curl
the endpoints.

---

## 7. Troubleshooting

| Symptom | Likely cause | Fix |
|---|---|---|
| `release version 17 not supported` | `mvn` using a JRE not a JDK | `export JAVA_HOME=~/.jdks/corretto-17.0.18` |
| `cannot find symbol` for a protected class | Phase 4 deleted the local copy but a caller still references it by simple name. | Add the canonical's import. The pipeline already auto-imports for known cases — file a bug if it missed yours. |
| `ConflictingBeanDefinitionException` for `xxx` | Two `@Component` classes share a default bean name. | The pipeline auto-uniquifies (subdomain prefix). If new conflict appears with an external library, add the canonical's `@Component("commonXxx")` per Rule 12. |
| `Could not resolve placeholder '${a.b.c}'` | A property key isn't in any `application-*.yml`. | Either include it in `application-<subdomain>.yml` (re-run Pipeline 5 after editing the source `.properties`) or add a default in the `@Value` (`${a.b.c:#{null}}`). |
| Gate 1 fails on `digit.models.coremodels` | False positive — that's an external library, not a banned local prefix. | The pipeline already excludes that pattern. If you see it, your branch is behind — `git pull origin monolith/main`. |
| Gate 2 fails (unresolved dups) | Phase 4 left dups and didn't list them as follow-ups. | Investigate the file — the canonical may have changed signature. Add to follow-ups manually if legitimate. |
| Gate 6 fails (db migrations) | Phase 9 was skipped via `--phase`, the source had no Flyway tree, or `dristi-app/application.yml` is missing the `classpath:/<subdomain>/db/migration/main` entry. | Re-run with `--phase 9` if the source actually has SQL. The phase auto-registers the Flyway location; if it warned that it couldn't, add it manually under `spring.flyway.locations:` in `dristi-app/application.yml`. |
| Gate 7 fails (flyway version uniqueness) | Two services chose the same `V<timestamp>` prefix (case/order/task all use `V20240424110535`). | Rename one file with an `_<n>` suffix on the version, e.g. `V20240424110535_2__order__ddl.sql`. Flyway parses underscores as decimal separators, so the renamed migration sorts after the original and the unified history table sees both as distinct versions. |
| Gate 8 fails (contract DTOs lifted) | Phase 35 was skipped via `--phase`, or a class slipped through the suffix/closure heuristic. | Re-run with `--phase 35`. If a leftover is genuinely contract-shaped but doesn't match any suffix, hand-curate the lift: copy the file to `dristi-common/contract/<subdomain>/`, add `// HAND-CURATED` as the first line, and delete the local copy. |
| Lifted contract DTO references `javax.validation.*` / `io.swagger.annotations.*` and `dristi-common` won't compile | Source services use pre-Jakarta + Swagger 1.x annotations on their DTOs. | The `dristi-common` pom carries `digit-models` + `swagger-core:1.5.18` for this reason. If new errors appear, the offending dep needs to be added the same way. Drop both deps once the codebase migrates to Jakarta. |
| Test fails with `WrongTypeOfReturnValue: <X> cannot be returned by <Y>()` | Mockito version drift in the parent pom (`mockito-core` was 3.12.4 vs. source services' 5.7.0). Symptom appears on `mock(JsonNode.class)`-style abstract-class mocks. See Rule 25. | Bump `mockito-core` in [dristi-monolith/pom.xml](../../dristi-monolith/pom.xml) to match the source services (currently 5.7.0). Propagate the bump into [scripts/migration/scaffold/02_generate_module_skeletons.py](scaffold/02_generate_module_skeletons.py) so it survives the next scaffold rebuild. |
| `cannot find symbol` for a deleted-protected-class method (e.g. `mdmsUtil.fetchMdmsData(...)` returning the wrong type) | Canonical's signature drifted from the service-local copy that was just deleted. See Rule 26 — companion to Rule 18. | Adapt the **caller** to the canonical's signature, not the canonical to the caller. Two patterns documented in Rule 26: direct use of the parsed structure, or re-serialize when the caller uses JsonPath. |
| `org.pucar.dristi.common.contract.<other-subdomain>.<X>` import appears in this subdomain's files after Phase 35 | Earlier auto-import bug — the cross-subdomain check now suppresses this. If you still see it on a fresh run, file a bug. | Strip the bad imports: `find … -name "*.java" \| xargs sed -i '/^import org\\.pucar\\.dristi\\.common\\.contract\\.<other>\\./d'`. Re-run `--phase 35` to repopulate correctly. |

For deeper detail on **why** the pipeline does what it does, see
[PIPELINE_RULES.md](PIPELINE_RULES.md) — every rule notes the original failure that
motivated it.

---

## 8. Commit + PR

**Three-commit structure** — see [PIPELINE_RULES.md](PIPELINE_RULES.md) Rule 28.
Each migration PR carries up to three commits:

### C1 — Structural lift

Pure file move (no contract moves yet) plus manual Tier 1 fixes.
Run the pipeline excluding Phase 35:

```bash
python3 scripts/migration/per_module/run_module_migration.py \
  --service <name> --module <module> --subdomain <subdomain> \
  --phase 1,2,3,4,5,6,7,8,9
```

Apply manual fixes (Flyway `_2` rename if Gate 7 collides; canonical
signature adapt per Rule 26; any parent-pom dep bumps per Rule 25).
Verify `mvn -pl domain-<module> -am test` green, then:

```bash
git add -A
git commit -m "migrate(<service>): structural lift to domain-<module>/<subdomain>

- N main + M test files moved from dristi-services/<service>
- K protected dups deduped to dristi-common
- Q follow-ups (see <service>_followups.txt)
- Per-controller @RequestMapping prefix /<context-path>
- Config: shared/<subdomain>/local profiles wired"
```

### C2 — Contract uplift + REST→direct calls

Run Phase 35, then convert any straightforward REST callers (Rule 27
decision tree). If lifted DTOs need legacy deps in `dristi-common`,
add them per Rule 24's note:

```bash
python3 scripts/migration/per_module/run_module_migration.py \
  --service <name> --module <module> --subdomain <subdomain> \
  --phase 35
# convert REST callers manually, see <service>_rest_calls.txt
mvn -pl domain-<module> -am test     # verify green

git add -A
git commit -m "refactor(<service>): contract uplift + REST→direct calls

- Lifted N contract DTOs to dristi-common/contract/<subdomain>/
- Converted M REST calls to direct method calls
  (targets: <list of migrated services>)
- dristi-common/pom.xml deps: <list>"
```

### C3 — Pipeline / rules / docs *(only if needed)*

Skip this commit unless this migration motivated changes to the
pipeline source, gate logic, or documentation. If yes:

```bash
git add scripts/migration/
git commit -m "feat(pipeline): <one-line description>

- <list of changes to run_module_migration.py / PIPELINE_RULES.md / etc.>"
```

### Push + PR

```bash
git push origin monolith/<service>
gh pr create --base monolith/main \
  --title "migrate: <service> → domain-<module>/<subdomain>" \
  --body "$(cat <<EOF
## Summary
- Source: \`dristi-services/<service>\` (X main + Y test files)
- Target: \`dristi-monolith/domain-<module>/.../<subdomain>/internal/\`
- Deduped: <list of protected classes>
- Lifted to dristi-common/contract/<subdomain>/: <count>
- Pipeline gates: all passed
- REST → direct conversions: <list> (see <service>_rest_calls.txt)

## Commits
- \`migrate(<service>): structural lift\` — pure file move + Tier 1 fixes
- \`refactor(<service>): contract uplift + REST→direct calls\` — Phase 35 + REST switches
- \`feat(pipeline): <change>\` *(if any pipeline/rules updates)*

## Follow-ups
- \`<service>_followups.txt\`: <list>
- \`<service>_rest_calls.txt\`: <count> entries pending until target services migrate
- \`<service>_contract_lift.txt\`: <count> classes lifted

## Test plan
- [ ] \`mvn -pl domain-<module> -am test\` green
- [ ] Local boot via \`scripts/local-test/README.md\`
- [ ] Endpoints reachable at \`/<context-path>/...\`
- [ ] QA sign-off
EOF
)"
```

---

## 9. Things you should NOT do

- **Don't edit auto-generated files structurally** (`dristi-monolith/pom.xml`,
  `application-shared.yml`, `application-<subdomain>.yml`). They're
  regenerated on every pipeline run; manual structural edits get clobbered.
  **Exception:** the `<dependencies>` section of `dristi-monolith/pom.xml`
  and `dristi-monolith/dristi-common/pom.xml` may carry hand-added deps
  (e.g. the `mockito-core` bump from Rule 25, the `digit-models` +
  `swagger-core:1.5.18` lines from Rule 24). The scaffold script's
  comment in those poms reads "Edit only the &lt;dependencies&gt; section".
  When you do edit, propagate the change into
  [scripts/migration/scaffold/02_generate_module_skeletons.py](scaffold/02_generate_module_skeletons.py)
  so the next scaffold rebuild keeps it.
- **Don't remove `spring.flyway.locations` entries by hand** in
  `dristi-app/application.yml` — Phase 9 appends one
  `classpath:/<subdomain>/db/migration/main` entry per migrated
  subdomain. Removing one without also pruning that subdomain's SQL
  tree under `domain-<module>/src/main/resources/<subdomain>/` will
  break boot when Flyway can't reconcile its history table.
- **Don't add files marked `// HAND-CURATED`** to the auto-generated
  list. The marker exists so re-runs preserve your edits.
- **Don't commit `target/` directories** (already in `.gitignore`).
- **Don't merge to `main` directly** — go through `monolith/main`. The
  cutover from `monolith/main` to `main` happens once, in Sprint 6,
  after QA passes for every service.
- **Don't bypass Gate 5** (`mvn validate`). It's the safety net.

---

## 10. What's in the repo for you

| Path | Purpose |
|---|---|
| [`scripts/migration/per_module/run_module_migration.py`](per_module/run_module_migration.py) | The 10-phase pipeline (incl. Phase 35 contract-lift) |
| [`scripts/migration/config_consolidation/run_consolidation.py`](config_consolidation/run_consolidation.py) | Pipeline 5 (config) |
| [`scripts/migration/dristi_common/`](dristi_common/) | One-time `dristi-common` extraction (already done) |
| [`scripts/migration/scaffold/`](scaffold/) | One-time monolith scaffold (already done) |
| [`scripts/migration/PIPELINE_RULES.md`](PIPELINE_RULES.md) | 23 hard-won rules — read it before debugging |
| [`scripts/migration/SERVICE_REGISTRY.md`](SERVICE_REGISTRY.md) | Service → module/subdomain mapping |
| [`scripts/migration/RUNBOOK.md`](RUNBOOK.md) | This file |
| [`scripts/local-test/README.md`](../local-test/README.md) | Local-boot smoke test recipe |

---

## TL;DR cheat sheet

The full per-service flow is **§8 Commit + PR** above (three-commit
structure per Rule 28). The recipe normally runs through the
`/migrate-service` slash command — see
[.claude/commands/migrate-service.md](../../.claude/commands/migrate-service.md).

```bash
# 0. branch
git checkout monolith/main && git pull && git checkout -b monolith/<service>

# 1. structural lift (commit C1)
python3 scripts/migration/per_module/run_module_migration.py \
  --service <name> --module <module> --subdomain <subdomain> \
  --phase 1,2,3,4,5,6,7,8,9      # (Phase 35 deferred to C2)
python3 scripts/migration/config_consolidation/run_consolidation.py \
  --service case --service lock-svc --service <name>
# add profile to dristi-app/.../application.yml: shared,cases,locksvc,<name>,local
mvn -pl domain-<module> -am test -Dsurefire.failIfNoSpecifiedTests=false
git add -A && git commit -m "migrate(<name>): structural lift to domain-<module>/<subdomain>"

# 2. contract uplift + REST→direct (commit C2)
python3 scripts/migration/per_module/run_module_migration.py \
  --service <name> --module <module> --subdomain <subdomain> --phase 35
# convert callers in <name>_rest_calls.txt where target is migrated
mvn -pl dristi-app -am test -Dsurefire.failIfNoSpecifiedTests=false
git add -A && git commit -m "refactor(<name>): contract uplift + REST→direct calls"

# 3. (optional) pipeline / rules / docs (commit C3)
git add scripts/migration/
git commit -m "feat(pipeline): <change>"

# 4. push + PR
git push -u origin monolith/<service>
gh pr create --base monolith/main
```
