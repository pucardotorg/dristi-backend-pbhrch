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
- `lock-svc`, `case`.

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

Single command per service — the pipeline runs all 8 phases (analyze →
scaffold → auto-rename → tests → deduplicate → REST detection → deps →
validate). Look up the `(module, subdomain)` for your service in
`SERVICE_REGISTRY.md`.

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
Phase 6 (test migration): copied 25 test files
Phase 4 (deduplicate): deleted=N protected-class copies, rewrote=N imports, ...
Phase 5 (REST detection): N intra-DRISTI candidate(s); see ..._rest_calls.txt
Phase 8: lifted N deps into dristi-monolith/domain-...pom.xml
PASS Gate 1: no banned-package imports
PASS Gate 2: no unresolved protected-class duplicates
PASS Gate 3: no module-level application.yml
PASS Gate 4: all target files in expected package
PASS Gate 5: dristi-common compiles
```

All five gates must `PASS`. If any **FAIL**, see [Troubleshooting](#7-troubleshooting).

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

### 5.3 Spot-check a controller and a service

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
| `ConflictingBeanDefinitionException` for `xxx` | Two `@Component` classes share a default bean name. | The pipeline auto-uniquifies (subdomain prefix). If new conflict appears with an external library, add the canonical's `@Component("commonXxx")` per Skill 12. |
| `Could not resolve placeholder '${a.b.c}'` | A property key isn't in any `application-*.yml`. | Either include it in `application-<subdomain>.yml` (re-run Pipeline 5 after editing the source `.properties`) or add a default in the `@Value` (`${a.b.c:#{null}}`). |
| Gate 1 fails on `digit.models.coremodels` | False positive — that's an external library, not a banned local prefix. | The pipeline already excludes that pattern. If you see it, your branch is behind — `git pull origin monolith/main`. |
| Gate 2 fails (unresolved dups) | Phase 4 left dups and didn't list them as follow-ups. | Investigate the file — the canonical may have changed signature. Add to follow-ups manually if legitimate. |

For deeper detail on **why** the pipeline does what it does, see
[SKILLS.md](SKILLS.md) — every skill notes the original failure that
motivated it.

---

## 8. Commit + PR

```bash
git add -A
git commit -m "migrate: <service> → domain-<module>/<subdomain>

- N main + M test files migrated
- K protected dups deduped to dristi-common
- Q follow-ups (see <service>_followups.txt)
- Per-controller @RequestMapping prefix /<context-path>
- Config: shared/<subdomain>/local profiles wired

Pipeline: scripts/migration/per_module/run_module_migration.py + Pipeline 5"

git push origin monolith/<service>
gh pr create --base monolith/main \
  --title "migrate: <service> → domain-<module>/<subdomain>" \
  --body "$(cat <<EOF
## Summary
- Source: \`dristi-services/<service>\` (X main + Y test files)
- Target: \`dristi-monolith/domain-<module>/.../<subdomain>/internal/\`
- Deduped: <list of protected classes>
- Pipeline gates: all passed
- Manual conversions done: <list of REST → method calls>

## Follow-ups
- \`<service>_followups.txt\`: <list>
- \`<service>_rest_calls.txt\`: <count> entries pending until target services migrate

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

- **Don't edit auto-generated files** (`dristi-monolith/pom.xml`,
  `application-shared.yml`, `application-<subdomain>.yml`). They're
  regenerated on every pipeline run; manual edits get clobbered.
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
| [`scripts/migration/per_module/run_module_migration.py`](per_module/run_module_migration.py) | The 8-phase pipeline |
| [`scripts/migration/config_consolidation/run_consolidation.py`](config_consolidation/run_consolidation.py) | Pipeline 5 (config) |
| [`scripts/migration/dristi_common/`](dristi_common/) | One-time `dristi-common` extraction (already done) |
| [`scripts/migration/scaffold/`](scaffold/) | One-time monolith scaffold (already done) |
| [`scripts/migration/SKILLS.md`](SKILLS.md) | 23 hard-won lessons — read it before debugging |
| [`scripts/migration/SERVICE_REGISTRY.md`](SERVICE_REGISTRY.md) | Service → module/subdomain mapping |
| [`scripts/migration/RUNBOOK.md`](RUNBOOK.md) | This file |
| [`scripts/local-test/README.md`](../local-test/README.md) | Local-boot smoke test recipe |

---

## TL;DR cheat sheet

```bash
# 1. branch
git checkout monolith/main && git pull && git checkout -b monolith/<service>

# 2. migrate
python3 scripts/migration/per_module/run_module_migration.py \
  --service <name> --module <module> --subdomain <subdomain>

# 3. consolidate config (include EVERY migrated service)
python3 scripts/migration/config_consolidation/run_consolidation.py \
  --service case --service lock-svc --service <name>

# 4. add the new profile name to dristi-app/.../application.yml
#    spring.profiles.active: shared,cases,locksvc,<name>,local

# 5. manual: convert REST calls listed in <name>_rest_calls.txt

# 6. build + test
mvn -pl dristi-app -am package
mvn -pl domain-<module> -am test -Dsurefire.failIfNoSpecifiedTests=false

# 7. PR to monolith/main
git add -A && git commit -m "migrate: <name>"
git push -u origin monolith/<service>
gh pr create --base monolith/main
```
