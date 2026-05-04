---
description: Diagnose a failing pipeline gate using PIPELINE_RULES.md and the pipeline source, then propose a fix per the tier rules.
argument-hint: <gate-number> [context]
---

# /debug-gate

Diagnose a single failing gate from the per-module migration pipeline.

**Arguments:** `$ARGUMENTS`
- `<gate-number>` — required, one of `1` through `7`.
- `[context]` — optional: error snippet, service name, or file path
  the failure mentions.

If `<gate-number>` is missing or out of range, stop and ask.

---

## Step 1 — Find relevant rule(s)

Grep PIPELINE_RULES.md for the gate number plus any keywords from the context:

```bash
grep -ni "Gate <N>\|gate <N>" scripts/migration/PIPELINE_RULES.md
# plus, if context mentions a class/file/symptom:
grep -ni "<symptom-keyword>" scripts/migration/PIPELINE_RULES.md
```

If the gate has no direct mention in PIPELINE_RULES.md (some gates are encoded
purely in the Python with no documented "Refined by reality"), grep
the pipeline source instead:

```bash
grep -n "phase_7_validate\|Gate <N>" scripts/migration/per_module/run_module_migration.py
```

Read **only the matching rule(s) and the matching Python function.**
Do not load all of PIPELINE_RULES.md.

---

## Step 2 — Verify the rule against current code

The rule describes how the gate was *designed* to behave. Confirm
the current implementation matches the rule's claim:

- Open the gate's Python function.
- Read the rule logic.
- Compare to what the rule says it does.
- If they diverge, the rule is stale — trust the code, flag the
  drift to the user.

---

## Step 3 — Diagnose

Decide which class of failure this is:

| Class | Example | Fix tier |
|---|---|---|
| **Stale rule** | Gate logic is buggy / over-broad | Tier 2 (propose pipeline edit) |
| **Missing whitelist entry** | New legitimate import / token / segment | Tier 2 (propose list edit) |
| **Genuine violation** | Migrated tree actually contains a banned thing | Tier 1 (fix in migrated tree) |
| **Novel failure mode** | Not covered by any existing rule | Tier 3 (lay out options) + propose new rule |

For each class, the action differs:

### Stale rule / missing whitelist entry (Tier 2)
- Show the diff to `run_module_migration.py` (or related script).
- Explain which rule the change extends ("per Rule 16, adding `<x>`
  to the digit-prefix whitelist").
- Wait for approval. Do not apply.

### Genuine violation (Tier 1)
- Identify the offending file in the migrated tree.
- Apply the correct fix per the rule (add an import, rename a
  Flyway file, add a HAND-CURATED marker, etc.).
- Show the diff. Re-run only that gate to confirm:
  ```bash
  python3 scripts/migration/per_module/run_module_migration.py \
    --service <svc> --module <m> --subdomain <s> --phase 7
  ```

### Novel failure mode (Tier 3)
- Lay out 2-3 options for fixing.
- Draft a candidate new rule (Rule / Refined by reality / Enforcement).
- Wait for user to choose option + confirm rule framing before
  any code or doc edit.

---

## Step 4 — Report

Tell the user:

1. **Which gate** failed and **which rule** applied (if any).
2. **What class** of failure it is (per Step 3 table).
3. **What was changed** (Tier 1 fix applied) **or proposed** (Tier 2/3).
4. **Re-run result** — gate now passing, or pending user approval.

If the fix is Tier 1 and the gate now passes, return control to
`/migrate-service` (or to the user if they invoked this command
standalone) so the migration can continue.

---

## Common gate → rule mapping (quick reference)

| Gate | Likely rules | Common cause |
|---|---|---|
| 1 (banned imports) | 8, 16 | False-positive on legitimate external library |
| 2 (protected dups) | 2, 13, 15, 18, 19 | Local helper kept that should be lifted, or vice versa |
| 3 (forbidden config) | 7 | Stray `application*.yml` in a domain module's main resources |
| 4 (package mismatch) | 1 | Phase 3 didn't rewrite a file's package line |
| 5 (compile) | 9 | Lombok annotation processing flake; missing canonical import |
| 6 (db migrations) | (in RUNBOOK §7) | Phase 9 skipped or Flyway location not registered |
| 7 (Flyway uniqueness) | (in RUNBOOK §7) | Two services chose the same `V<timestamp>` |

This table is a starting point — always grep first, don't assume.
