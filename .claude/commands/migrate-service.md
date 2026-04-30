---
description: Run the per-module migration pipeline end-to-end with pre-flight, manual review, config consolidation, and build verification.
argument-hint: <service> <module> <subdomain>
---

# /migrate-service

Migrate one DRISTI service into the modular monolith using the standard
9-phase pipeline + config consolidation + build verification.

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

## Step 1 — Pre-flight (Tier 1)

Run these checks. If any fails, stop and surface to the user:

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

## Step 2 — Run the pipeline (Tier 1)

```bash
python3 scripts/migration/per_module/run_module_migration.py \
  --service <service> --module <module> --subdomain <subdomain> \
  2>&1 | tee /tmp/migration-<service>.log
```

If the run takes >2 min, use `run_in_background: true` and monitor.

When complete, parse the gate summary:

```bash
grep -E "^(PASS|FAIL) Gate" /tmp/migration-<service>.log
```

**If any gate fails:** invoke the `/debug-gate` workflow for that gate.
Do not proceed past Step 2 with a failing gate.

---

## Step 3 — Read manual-review files (Tier 1)

```bash
cat scripts/migration/per_module/output/<service>_rest_calls.txt 2>/dev/null
cat scripts/migration/per_module/output/<service>_followups.txt 2>/dev/null
```

For each non-empty file, classify each entry:

### REST calls
- **Tier 1 to convert:** target service is already in the monolith
  (currently `case`, `lock-svc`) AND the call shape is straightforward
  (typed DTO in/out). Do the conversion in the migrated tree, show
  the diff.
- **Tier 4 (ask user):** target service is unmigrated, or the call
  uses `Object`-typed payloads, or the host getter name is ambiguous.
  List these and ask which to convert.
- **Skip:** target is a platform service (eGov / DIGIT). Skill 17 lists
  the tokens. Confirm in chat which were skipped and why.

### Follow-ups (kept protected-class files)
- **Tier 3 decision per file:** lift extra methods into the canonical
  in `dristi-common` vs. keep as service-local helper. Lay out the
  tradeoff per file (consult Skills 13, 15, 18, 19), wait for user
  decision before any pipeline-level edit.

---

## Step 4 — Config consolidation (Tier 1)

Determine the full list of services already in the monolith plus the
new one. Currently migrated: `case`, `lock-svc`. Add `<service>`.

```bash
python3 scripts/migration/config_consolidation/run_consolidation.py \
  --service case --service lock-svc --service <service>
```

Then edit `dristi-app/src/main/resources/application.yml` to add the
new subdomain to `spring.profiles.active`:

```yaml
spring:
  profiles:
    active: shared,cases,locksvc,<subdomain>,local
```

Read the conflict report:

```bash
cat scripts/migration/config_consolidation/output/config_conflicts.txt 2>/dev/null
```

Report any conflicts to the user. Spring's profile-overlay order
usually resolves them; flag any that look behaviorally significant.

---

## Step 5 — Build verification (Tier 1)

```bash
JAVA_HOME=/home/mani/.jdks/corretto-17.0.18 \
  PATH=/home/mani/.jdks/corretto-17.0.18/bin:$PATH \
  mvn -B -pl domain-<module> -am test \
  -Dsurefire.failIfNoSpecifiedTests=false \
  2>&1 | tail -200 > /tmp/mvn-test-<service>.log
echo "exit=$?"
```

If exit != 0, grep for `[ERROR]` lines first. Only read full files
when investigating a specific failure. Failures here are real — do
not handwave.

If tests pass, build the fat JAR:

```bash
mvn -B -pl dristi-app -am package 2>&1 | tail -100 > /tmp/mvn-package-<service>.log
echo "exit=$?"
```

---

## Step 6 — Summary and pause

Print a concise summary:

- **Migrated:** N main + M test files into `domain-<module>/<subdomain>`
- **Deduplicated:** list of protected classes removed
- **Auto-fixed (Tier 1):** REST conversions made, Flyway renames, etc.
- **Awaiting user decision:** any Tier 2/3/4 items
- **Build:** PASS / FAIL with one-line cause if failed
- **Gates:** all PASS / list of any FAIL

Then **stop**. Do not commit, push, or open a PR without explicit
"ship it" from the user. When the user confirms, suggest the commit +
PR sequence per RUNBOOK §8.

---

## Hard stops during this command

- Failing gate at Step 2 → invoke `/debug-gate`, return when fixed.
- Tier 2/3/4 decision needed → present, wait, do not act.
- `mvn` failure at Step 5 → diagnose, do not commit.
- User says "stop" or shows hesitation → stop immediately.
