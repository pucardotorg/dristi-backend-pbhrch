/**
 * Task subdomain — task lifecycle for orders/summons/warrants tied to a case.
 *
 * <p>Marked as a Spring Modulith application module. Other subdomains
 * MUST consume task through
 * {@link org.pucar.dristi.caselifecycle.task.TaskApi}; reaching into
 * {@code internal/} is a structural violation enforced by
 * {@code ModuleStructureTest.verify()}.
 *
 * <p><b>Cycle-break history:</b> a {@code TaskApi} was first introduced
 * by the task migration, then removed in commit {@code a89087936} (C3)
 * to break the {@code cases→task→order→cases} Spring DI cycle —
 * {@code cases→task} reverted to REST. Re-introduced by the
 * order-management migration for downstream-only consumption: the
 * original cycle stays broken because cases still calls task via REST.
 *
 * <p>Contract DTOs (HTTP wire format) live in
 * {@code dristi-common/contract/task/} per Rule 24 (Phase 35 lift).
 */
@org.springframework.modulith.NamedInterface
@org.springframework.modulith.ApplicationModule(displayName = "Task")
package org.pucar.dristi.caselifecycle.task;
