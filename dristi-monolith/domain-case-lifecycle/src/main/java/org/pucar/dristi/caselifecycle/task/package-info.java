/**
 * Task subdomain — task lifecycle for orders/summons/warrants tied to a case.
 *
 * <p>Marked as a Spring Modulith application module so cross-subdomain
 * boundaries inside {@code domain-case-lifecycle} are enforced by
 * {@code ModuleStructureTest.verify()}. No cross-subdomain {@code *Api}
 * is currently exposed — callers reach task via REST.
 *
 * <p>Contract DTOs (HTTP wire format) live in
 * {@code dristi-common/contract/task/} per Rule 24 (Phase 35 lift).
 */
@org.springframework.modulith.ApplicationModule(displayName = "Task")
package org.pucar.dristi.caselifecycle.task;
