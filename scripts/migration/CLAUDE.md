# Migration session rules

This file loads automatically when Claude reads or edits any file under
`scripts/migration/`. It encodes the rules every Claude session in this
subtree must follow. Recipes themselves live in slash commands — see
the bottom of this file.

The companion human docs are [RUNBOOK.md](RUNBOOK.md) (operational guide)
and [PIPELINE_RULES.md](PIPELINE_RULES.md) (debug archive).

---

## 1. Entry points (do not improvise)

| Task | Use |
|---|---|
| Migrate a service end-to-end | `/migrate-service <service> <module> <subdomain>` |
| Diagnose a single gate failure | `/debug-gate <gate-number> [context]` |
| Anything else (ad-hoc questions, exploration) | Direct prompt is fine |

**Do not run `run_module_migration.py` ad-hoc.** Always go through
`/migrate-service` so pre-flight checks, manual-review parsing, and the
build verification all happen in the standard order.

If the user asks "migrate service X" without using the slash command,
suggest it first: *"Run `/migrate-service X <module> <subdomain>` so the
standard recipe applies."*

---

## 2. Escalation tiers

Every action Claude takes during migration work falls into one of four
tiers. The tier determines whether to act, propose, or stop.

### Tier 1 — Act, narrate one sentence
Local, reversible, low-risk inside the migrated tree.

- Pre-flight checks (`git status`, branch, source dir).
- Running the pipeline; running config consolidation; running `mvn`.
- Reading any `_followups.txt`, `_rest_calls.txt`, `_contract_lift.txt`,
  `config_conflicts.txt`.
- Adding a `// HAND-CURATED` marker to a file Claude just edited.
- Adding a `@Value` default (`${a.b.c:#{null}}`) per RUNBOOK §7.
- Renaming a Flyway file to `V<ts>_2__...sql` to break a Gate 7 collision.
- Adapting a service-local caller to the canonical's method signature
  (Rule 26) — e.g. switching `String → readValue` chains to direct use
  of the canonical's parsed return type.
- Converting an intra-DRISTI REST call to a direct service call **inside
  the migrated tree** when the target service is already in the monolith
  AND the conversion is genuinely behaviour-preserving (Rule 27 default
  is "follow-up PR" — only deviate for the easy cases).

### Tier 2 — Propose diff, wait for approval
Edits to the **pipeline source** or its data lists. Affects every
future migration.

- Edits to `scripts/migration/per_module/run_module_migration.py`.
- Whitelist/blocklist tweaks (banned imports, `EGOV_HOST_TOKENS`,
  `DOMAIN_MODULE_SEGMENTS`, `CONTRACT_SUFFIXES`).
- New gate logic; new phase; phase reordering.
- Edits to `scripts/migration/config_consolidation/run_consolidation.py`.
- Bumping a version pin in `dristi-monolith/pom.xml` or
  `dristi-monolith/dristi-common/pom.xml` (Rule 25). Affects every
  module's resolved classpath.
- Adding a dep to `dristi-monolith/dristi-common/pom.xml` (Rule 24:
  e.g. `digit-models`, `swagger-core:1.5.18` to satisfy lifted contract
  DTOs' legacy imports).

Show the diff, explain why, wait. Do not apply.

### Tier 3 — Lay out 2-3 options with tradeoffs, wait
Architectural / cross-service decisions where wrong-answer cost is high.

- Widening a canonical method signature to `Object` (Rule 18) — only
  for request-side divergence; return-type drift is Tier 1 (Rule 26:
  adapt the caller).
- Lifting a kept helper into `dristi-common` vs. keeping it
  service-local (Rule 13).
- Promoting a contract DTO out of `dristi-common/contract/<subdomain>/`
  into a shared `dristi-common/contract/<shared>/` namespace once two
  services prove they want the same concrete type (Rule 24).
- Adding a class to the lift set that doesn't match `CONTRACT_SUFFIXES`
  (false negative — would the caller need it for a direct call?).
- Resolving a CONFLICT-classified config key when behavioral
  divergence matters.
- Modifying PIPELINE_RULES.md (writing a new rule, retiring an old one).

### Tier 4 — Ask the user
Domain knowledge Claude can't supply.

- "Is this REST host a DRISTI service or a platform service?"
- "Is this `@Value` default intentional?"
- "Should we ship this before sprint cutoff?"
- "Has the dependency `<X>` been merged to `monolith/main`?"

---

## 3. PIPELINE_RULES.md usage

[PIPELINE_RULES.md](PIPELINE_RULES.md) is a debug archive, not runtime
config. Consult it **only on failure or ambiguity**, not preemptively.

### When to consult

- A pipeline gate prints `FAIL`.
- A manual-review file (`_followups.txt`, `_rest_calls.txt`) contains
  an entry whose right answer isn't obvious.
- Compile fails with a symptom that doesn't match RUNBOOK §7's table.
- Boot fails with a symptom that doesn't match RUNBOOK §7's table.

### How to use

1. **Search** PIPELINE_RULES.md by gate number, symptom keyword, or class name:
   ```bash
   grep -ni "Gate 2\|placeholder\|IndividualUtil" scripts/migration/PIPELINE_RULES.md
   ```
2. **Read only the matching rule(s).** Rules are independent.
3. **Verify against current Python** before acting. Rules describe
   how the pipeline *was designed*; the code may have drifted.
4. **Apply per tier rules.** Most rule-driven fixes are Tier 1
   (input fix) or Tier 2 (extending a list in the pipeline).
5. **Tell the user which rule applied** in the session summary.

### When to write a new rule

If Claude debugs a novel failure that isn't covered by any existing
rule *and* the fix changes the pipeline:

1. Draft the new rule in chat using the existing template (Rule /
   Refined by reality / Enforcement).
2. Wait for user confirmation that the framing is correct.
3. Then add it to PIPELINE_RULES.md.

Do not silently append to PIPELINE_RULES.md mid-session.

---

## 4. Output handling conventions

Pipeline and Maven output can be large. To keep context efficient:

- **Tee long-running commands to `/tmp/`:**
  ```bash
  python3 scripts/migration/per_module/run_module_migration.py ... \
    2>&1 | tee /tmp/migration-<service>.log
  ```
- **Grep summaries first, drill in second:**
  ```bash
  grep -E "^(PASS|FAIL) Gate" /tmp/migration-<service>.log
  ```
- **Tail Maven logs** instead of reading them whole:
  ```bash
  mvn -B -pl <module> -am test ... 2>&1 | tail -200 > /tmp/mvn-<service>.log
  grep "\[ERROR\]" /tmp/mvn-<service>.log
  ```
- **Long pipeline runs** (>2 min): use `run_in_background: true` and
  monitor for completion rather than blocking.

---

## 5. Hard rules (never do these without explicit user instruction)

- **Do not edit auto-generated files structurally:**
  - `dristi-monolith/pom.xml` (parent — only the `<dependencies>` and
    `<dependencyManagement>` sections are hand-editable; see Rule 25)
  - `dristi-monolith/dristi-common/pom.xml` (only the `<dependencies>`
    section is hand-editable; see Rule 24's note on `digit-models` +
    `swagger-core:1.5.18`)
  - `dristi-app/src/main/resources/application-shared.yml`
  - `dristi-app/src/main/resources/application-<subdomain>.yml`
  Re-running the pipeline regenerates structure; manual structural edits
  get clobbered. When you legitimately add a dep to one of the editable
  pom sections, also propagate it into
  `scripts/migration/scaffold/02_generate_module_skeletons.py` so the
  next scaffold regen carries it forward.

- **Do not remove `spring.flyway.locations` entries** in
  `dristi-app/.../application.yml` by hand — Phase 9 owns that list.

- **Do not modify files marked `// HAND-CURATED`** without the user
  acknowledging the marker. The marker exists so re-runs preserve
  human edits; respect it.

- **Do not commit without a pre-commit summary first** (Rule 30).
  Every `git commit` on a migration session must be preceded by a
  structured summary of files-added / files-deleted / files-modified /
  decisions-taken / verifications-not-yet-run, and the user must
  confirm the design before verification + commit run. Skip the
  summary only for trivial single-purpose commits the user explicitly
  authorised mid-session (e.g. `/migrate-service` Step 6's "ship it"
  flow has its own summary).

- **Do not push or open PRs** without explicit user confirmation,
  even after a green build.

- **Do not bypass any gate** (`--no-verify`, deleting validation,
  skipping `--phase 7`). Gates are the safety net.

- **Do not merge to `main`** — all migration work flows through
  `monolith/main`. Cutover is a separate ceremony.

---

## 6. Reference

| Path | Purpose |
|---|---|
| [RUNBOOK.md](RUNBOOK.md) | Human-readable operational guide |
| [PIPELINE_RULES.md](PIPELINE_RULES.md) | 30 hard-won rules, indexed by gate/symptom (24=contract lift, 25=parent pom dep hygiene, 26=canonical return-type drift, 27=REST→direct as follow-up PR, 28=three-commit structure, 29=workflow migration pattern + behavior-union extraction, 30=pre-commit summary protocol) |
| [SERVICE_REGISTRY.md](SERVICE_REGISTRY.md) | Service → module/subdomain mapping |
| [per_module/run_module_migration.py](per_module/run_module_migration.py) | The 9-phase pipeline |
| [config_consolidation/run_consolidation.py](config_consolidation/run_consolidation.py) | Pipeline 5 (config) |
| [.claude/commands/migrate-service.md](../../.claude/commands/migrate-service.md) | Migration recipe (slash command) |
| [.claude/commands/debug-gate.md](../../.claude/commands/debug-gate.md) | Gate debug recipe (slash command) |
