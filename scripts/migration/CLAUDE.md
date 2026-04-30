# Migration session rules

This file loads automatically when Claude reads or edits any file under
`scripts/migration/`. It encodes the rules every Claude session in this
subtree must follow. Recipes themselves live in slash commands — see
the bottom of this file.

The companion human docs are [RUNBOOK.md](RUNBOOK.md) (operational guide)
and [SKILLS.md](SKILLS.md) (debug archive).

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
- Reading any `_followups.txt`, `_rest_calls.txt`, `config_conflicts.txt`.
- Adding a `// HAND-CURATED` marker to a file Claude just edited.
- Adding a `@Value` default (`${a.b.c:#{null}}`) per RUNBOOK §7.
- Renaming a Flyway file to `V<ts>_2__...sql` to break a Gate 7 collision.
- Converting an intra-DRISTI REST call to a direct service call **inside
  the migrated tree** when the target service is already in the monolith.

### Tier 2 — Propose diff, wait for approval
Edits to the **pipeline source** or its data lists. Affects every
future migration.

- Edits to `scripts/migration/per_module/run_module_migration.py`.
- Whitelist/blocklist tweaks (banned imports, `EGOV_HOST_TOKENS`,
  `DOMAIN_MODULE_SEGMENTS`).
- New gate logic; new phase; phase reordering.
- Edits to `scripts/migration/config_consolidation/run_consolidation.py`.

Show the diff, explain why, wait. Do not apply.

### Tier 3 — Lay out 2-3 options with tradeoffs, wait
Architectural / cross-service decisions where wrong-answer cost is high.

- Widening a canonical method signature to `Object` (Skill 18).
- Lifting a kept helper into `dristi-common` vs. keeping it
  service-local (Skill 13).
- Resolving a CONFLICT-classified config key when behavioral
  divergence matters.
- Modifying SKILLS.md (writing a new skill, retiring an old one).

### Tier 4 — Ask the user
Domain knowledge Claude can't supply.

- "Is this REST host a DRISTI service or a platform service?"
- "Is this `@Value` default intentional?"
- "Should we ship this before sprint cutoff?"
- "Has the dependency `<X>` been merged to `monolith/main`?"

---

## 3. SKILLS.md usage

[SKILLS.md](SKILLS.md) is a debug archive, not runtime config. Consult
it **only on failure or ambiguity**, not preemptively.

### When to consult

- A pipeline gate prints `FAIL`.
- A manual-review file (`_followups.txt`, `_rest_calls.txt`) contains
  an entry whose right answer isn't obvious.
- Compile fails with a symptom that doesn't match RUNBOOK §7's table.
- Boot fails with a symptom that doesn't match RUNBOOK §7's table.

### How to use

1. **Search** SKILLS.md by gate number, symptom keyword, or class name:
   ```bash
   grep -ni "Gate 2\|placeholder\|IndividualUtil" scripts/migration/SKILLS.md
   ```
2. **Read only the matching skill(s).** Skills are independent.
3. **Verify against current Python** before acting. Skills describe
   how the pipeline *was designed*; the code may have drifted.
4. **Apply per tier rules.** Most skill-driven fixes are Tier 1
   (input fix) or Tier 2 (extending a list in the pipeline).
5. **Tell the user which skill applied** in the session summary.

### When to write a new skill

If Claude debugs a novel failure that isn't covered by any existing
skill *and* the fix changes the pipeline:

1. Draft the new skill in chat using the existing template (Rule /
   Refined by reality / Enforcement).
2. Wait for user confirmation that the framing is correct.
3. Then add it to SKILLS.md.

Do not silently append to SKILLS.md mid-session.

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

- **Do not edit auto-generated files:**
  - `dristi-monolith/pom.xml`
  - `dristi-app/src/main/resources/application-shared.yml`
  - `dristi-app/src/main/resources/application-<subdomain>.yml`
  Re-running the pipeline regenerates them; manual edits are clobbered.

- **Do not remove `spring.flyway.locations` entries** in
  `dristi-app/.../application.yml` by hand — Phase 9 owns that list.

- **Do not modify files marked `// HAND-CURATED`** without the user
  acknowledging the marker. The marker exists so re-runs preserve
  human edits; respect it.

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
| [SKILLS.md](SKILLS.md) | 23 hard-won lessons, indexed by gate/symptom |
| [SERVICE_REGISTRY.md](SERVICE_REGISTRY.md) | Service → module/subdomain mapping |
| [per_module/run_module_migration.py](per_module/run_module_migration.py) | The 9-phase pipeline |
| [config_consolidation/run_consolidation.py](config_consolidation/run_consolidation.py) | Pipeline 5 (config) |
| [.claude/commands/migrate-service.md](../../.claude/commands/migrate-service.md) | Migration recipe (slash command) |
| [.claude/commands/debug-gate.md](../../.claude/commands/debug-gate.md) | Gate debug recipe (slash command) |
