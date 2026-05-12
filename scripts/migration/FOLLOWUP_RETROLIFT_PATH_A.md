# Follow-up: Retro-lift case + lock-svc Contracts to `dristi-common`

**Status:** Deferred. Path B (`@NamedInterface("contract")` on
`<subdomain>/internal/web/models/`) shipped in commit `7790b59c2`.
This doc captures everything a future session needs to pick up Path
A — the proper relocation of contract DTOs to
`dristi-common/contract/<subdomain>/`.

**Why deferred:** the trial Phase 35 retro-lift on `case` produced
**203 compile errors** spanning four error categories Phase 35 cannot
resolve mechanically. Fixing each category is meaningful pipeline +
source surgery; bundling all of it into the LockApi/CaseApi cutover
PR would have multiplied the scope by 5-10×. The named-interface
pattern (Rule 24a) achieves the same architectural outcome
(cross-module API boundary, no `internal/` leakage) without the
pipeline work.

---

## What was attempted (so the next session can reproduce or skip)

```bash
python3 scripts/migration/per_module/run_module_migration.py \
  --service lock-svc --module case-lifecycle --subdomain locksvc \
  --phase 35
# → lifted 5 classes to dristi-common/contract/locksvc/
#   compile errors: 5 (all in Lock.java — JPA annotations)

python3 scripts/migration/per_module/run_module_migration.py \
  --service case --module case-lifecycle --subdomain cases \
  --phase 35
# → lifted 140 classes to dristi-common/contract/cases/
#   rewrote imports in 109 files
#   compile errors: 203
```

Both were rolled back via `git checkout -- .` and `rm -rf
dristi-common/.../contract/{cases,locksvc}/`.

---

## The four error categories

These came out of the case run; lock-svc hit only category A.

### A. Dual-role classes — JPA entity AND wire DTO

Classes carrying both `@Entity`/`@Table`/`@Id`/`@Column` AND serving
as the HTTP wire format. After lifting to `dristi-common`:

- `dristi-common`'s pom doesn't pull in `jakarta.persistence-api`, so
  `@Entity` etc. don't resolve.
- The JPA repository (`LockRepository extends JpaRepository<Lock,
  UUID>`) expects the entity in lock-svc's package. Spring's
  `@EntityScan` configuration is keyed on the original location; the
  scan no longer finds Lock.

**Concrete instances:** `Lock` (lock-svc), `CourtCase`, `Address`,
`Litigant`, `Representative` (case).

**Fix paths:**

1. **Split.** Separate the JPA entity (stays in `internal/repository/`
   or `internal/persistence/`) from the wire DTO (lifts to
   `dristi-common/contract/`). Mapper at the boundary. Real but
   meaningful refactor — touches each dual-role class one-by-one.
2. **Add jakarta.persistence-api to dristi-common pom.** Cheaper but
   wrong: contract DTOs shouldn't be persistence-aware. The runtime
   problem (JPA scan path) still has to be solved separately.

(1) is the right path. Plan to do this before Path A lift.

### B. Subpackage tendrils

`Address.java` imports `cases.internal.web.models.v2.AddressV2`. Phase
35's file walk only looks at `web/models/*.java` (top level); the
subpackage types stay put. Lifted `Address` (now at
`dristi-common/contract/cases/Address`) can't see `AddressV2` because
the subpackage is in another Maven module and not exported.

Case's `web/models/` subpackages: `v2`, `advocateDetails`,
`advocateoffice`, `enums`, `order`, `inbox`, `analytics`,
`individualDetails`.

**Fix path:** Phase 35 walks subpackages too. The lifted destination
mirrors the source structure — `web/models/v2/AddressV2` →
`contract/cases/v2/AddressV2`. Imports rewritten correspondingly.
Bigger sweep, more risk of accidental promotion of types that
shouldn't be in dristi-common.

### C. References to `internal/annotation/` and `internal/web/`

Custom Bean Validation annotations and base classes referenced from
contract DTOs. Lifted DTO can't see them across the Maven boundary.

**Concrete instances:** `CaseSearchCriteria` →
`cases.internal.annotation.SomeValidator`,
`OpenApiCaseSummaryResponse` → `cases.internal.web.SomeBase`.

**Fix paths:**

1. **Lift annotation/base classes too.** Their packages get
   `@NamedInterface` markers if they need cross-module visibility.
   Bigger scope.
2. **Strip the references from lifted DTOs.** Loses validation /
   shared base behaviour. Not acceptable for live code.
3. **Mark the source `internal/annotation/` package with
   `@NamedInterface`** and import from there in the lifted DTO. Hybrid
   — the contract DTO is in dristi-common but references in-place
   annotation; cross-module dependency back. Smells.

(1) is cleanest. Audit each `internal/annotation` symbol used by
contract DTOs and migrate the reachable set.

### D. External library deps not in dristi-common's pom

`IndividualSearch.java` uses `org.egov.common.data.query.annotations.*`
— that artifact wasn't in `dristi-common`'s dependency list. Add it.
Likely more will surface as we iterate.

**Fix path:** add to `dristi-common/pom.xml`. Each addition expands
dristi-common's classpath; review whether the dep is appropriate at
that layer (vs in domain modules only).

---

## What Path A buys us once it lands

- All migrated services follow the **same** Rule 24 pattern. No
  decision tree at migration time ("is your web/models tangled?
  apply 24a; clean? apply 24"). Single shape to teach new contributors.
- Contract DTOs aggregate in one place (`dristi-common/contract/`).
  Easier to find, easier to share between services.
- Phase 35's robustness improvements benefit every subsequent
  migration.

What Path A *doesn't* buy: the architectural boundary. That's already
in place via Path B's `@NamedInterface`. Cross-module imports are
already enforced by `ModuleStructureTest.verify()`. Path A is
relocation + cleanup, not new capability.

---

## Step-by-step plan for the future session picking this up

### Phase 0 — Confirm the patterns still hold

The codebase will have evolved. Re-check:

```bash
# How many contract-suffixed classes does case have?
ls dristi-monolith/domain-case-lifecycle/src/main/java/org/pucar/dristi/caselifecycle/cases/internal/web/models/ | wc -l

# Do subpackages exist?
ls dristi-monolith/domain-case-lifecycle/src/main/java/org/pucar/dristi/caselifecycle/cases/internal/web/models/

# Does Lock still carry @Entity?
grep -E "@Entity|@Table" dristi-monolith/domain-case-lifecycle/src/main/java/org/pucar/dristi/caselifecycle/locksvc/internal/web/models/Lock.java
```

If the categories below look different (e.g. someone already split
the dual-role classes), update this doc and the rules accordingly.

### Phase 1 — Source-side surgery (Category A first)

Split each dual-role class. Suggested order: lock-svc.Lock first
(small, single class), then case's CourtCase (the central one),
then the rest.

Per class:

1. Create `<svc>/internal/persistence/<X>Entity.java` with
   `@Entity`/`@Table`/`@Id`/`@Column`/JPA mapping annotations.
2. Strip JPA annotations from the wire-format `<X>.java` (which will
   later lift). Keep field structure.
3. Add a mapper (`<X>Mapper.java` or simple `toEntity()`/`toDto()`
   methods).
4. Rewrite the JPA repository (`<X>Repository extends
   JpaRepository<<X>Entity, ...>`) and any service code that touches
   persisted state.
5. `mvn test -pl <module> -am` between each.

### Phase 2 — Pipeline robustness (Phase 35 enhancements)

In `scripts/migration/per_module/run_module_migration.py`:

- **Subpackage walk** (Category B): `phase_35_contract_lift` extends
  its file walk to `web_models_dir.rglob("*.java")` instead of
  `glob("*.java")`. Destination mirrors source: `web/models/v2/X` →
  `contract/<subdomain>/v2/X`. The lift map handles imports.
- **Internal annotation handling** (Category C): a separate detection
  step finds references from contract DTOs into `internal/annotation/`
  or `internal/web/`. For each, the option is "lift the referenced
  package too" or "stamp `@NamedInterface` on the source package".
  Add a flag to choose; default to lift.
- **Dependency expansion** (Category D): add a check that resolves
  every import of a lifted class against `dristi-common`'s effective
  classpath. Print missing deps; let the operator add them to
  `dristi-common/pom.xml` before re-running.

### Phase 3 — Re-run Phase 35 on case + lock-svc

```bash
# Idempotent re-run; existing @NamedInterface marker on the source
# package gets removed at the end (see Phase 4).
python3 scripts/migration/per_module/run_module_migration.py \
  --service lock-svc --module case-lifecycle --subdomain locksvc \
  --phase 35
python3 scripts/migration/per_module/run_module_migration.py \
  --service case --module case-lifecycle --subdomain cases \
  --phase 35
mvn -pl domain-case-lifecycle -am test
```

### Phase 4 — Transition Path B → Path A

1. **Update `*Api` imports.** `LockApi`, `CaseApi`, `OrderApi` and
   their impls have method signatures referring to
   `<subdomain>.internal.web.models.<X>`. Change to
   `org.pucar.dristi.common.contract.<subdomain>.<X>`. Phase 35's
   import-rewrite sweep should do most of this automatically; verify
   manually.
2. **Update caller imports.** Every `@Autowired *Api` consumer that
   imports a contract DTO via the named interface switches to
   `dristi-common/contract/`. Phase 35 sweep handles this.
3. **Remove the `@NamedInterface("contract")` marker** from
   `<subdomain>/internal/web/models/package-info.java`. The package is
   empty (or near-empty) post-lift; the marker becomes dead.
4. **Delete empty `web/models/` directories.** The pipeline's
   `_prune_empty_dirs` (in `run_module_migration.py`) handles this on
   subsequent runs.

### Phase 5 — Update rules + remove this doc

- **Rule 24** loses the "case and lock-svc were migrated before Phase
  35 existed" note — they're now lifted.
- **Rule 24a** stays as documentation for any future service whose
  `web/models/` is too tangled even after the Phase 35 enhancements.
  If no service ever needs it, retire the rule.
- Delete this doc; reference becomes stale.

---

## Cost estimate

- Phase 1 (entity/DTO split): ~1-2 hours per service. Probably 4-6
  classes need splitting across case + lock-svc. Total ~6-12 hours.
- Phase 2 (pipeline enhancements): ~3-5 hours, depending on how clean
  the current pipeline is.
- Phase 3 (re-run): minutes.
- Phase 4 (transition): ~1 hour, mostly verifying the sweep didn't miss
  anything.
- Phase 5 (rules): ~30 min.

**Total: ~10-18 hours of focused work.** Best done in one focused PR
since the pipeline changes touch every future migration; doing them
in pieces creates intermediate states with "fixed for case but not for
hearing" gotchas.

---

## What NOT to do

1. **Don't lift case into `dristi-common/contract/cases/` without the
   Phase 1 entity split.** You'll hit Category A errors and have to
   roll back. The trial in commit `7790b59c2`'s history confirmed this
   the hard way.
2. **Don't add `jakarta.persistence-api` to `dristi-common`'s pom.**
   It "fixes" Category A's compile errors but bakes persistence
   awareness into a shouldn't-be-persistence-aware module. The right
   fix is the entity split.
3. **Don't drop the `@NamedInterface("contract")` marker before
   Phase 4 step 2 is complete.** Cross-module callers still use the
   named interface during the migration window; removing it early
   breaks `ModuleStructureTest.verify()`.

---

## Reference

- Original commit attempting Path A (rolled back, never committed):
  see [PIPELINE_RULES.md Rule 24a](PIPELINE_RULES.md#rule-24a--namedinterfacecontract-when-retro-lift-isnt-feasible).
- Path B that shipped: commit `7790b59c2` — `feat(modulith):
  per-subdomain @ApplicationModule + LockApi/CaseApi cutover`.
- Spring Modulith reference for the `@ApplicationModule` /
  `@NamedInterface` annotations:
  <https://docs.spring.io/spring-modulith/reference/fundamentals.html>.
