/**
 * Task subdomain — task lifecycle for orders/summons/warrants tied to a case.
 *
 * <p>Marked as a Spring Modulith application module so cross-subdomain
 * boundaries inside {@code domain-case-lifecycle} are enforced by
 * {@code ModuleStructureTest.verify()}. Other subdomains MUST consume
 * task through {@link org.pucar.dristi.caselifecycle.task.TaskApi};
 * reaching into {@code internal/} is a structural violation.
 *
 * <p><b>Cycle-break history:</b> a {@code TaskApi} was first introduced
 * by the task migration, then removed in commit {@code a89087936} (C3)
 * to break the {@code cases→task→order→cases} Spring DI cycle —
 * {@code cases→task} reverted to REST. Re-introduced by the
 * order-management and casemanagement migrations for downstream-only
 * consumption: the active callers are sink-nodes in the dependency
 * graph (no peer subdomain calls back into them), so no cycle can
 * re-form; the original cycle stays broken because cases still calls
 * task via REST.
 *
 * <p>Contract DTOs (HTTP wire format) live in
 * {@code dristi-common/contract/task/} per Rule 24 (Phase 35 lift).
 */
@org.springframework.modulith.ApplicationModule(displayName = "Task")
@org.springframework.modulith.NamedInterface("api")
package org.pucar.dristi.caselifecycle.task;
