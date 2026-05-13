/**
 * Task subdomain — task lifecycle for orders/summons/warrants tied to a case.
 *
 * <p>Marked as a Spring Modulith application module so cross-subdomain
 * boundaries inside {@code domain-case-lifecycle} are enforced by
 * {@code ModuleStructureTest.verify()}. Other subdomains MUST consume task
 * through {@link TaskApi}; reaching into {@code internal/} is a structural
 * violation.
 *
 * <p>Contract DTOs (HTTP wire format) live in
 * {@code dristi-common/contract/task/} per Rule 24 (Phase 35 lift).
 */
@org.springframework.modulith.ApplicationModule(displayName = "Task")
@org.springframework.modulith.NamedInterface("api")
package org.pucar.dristi.caselifecycle.task;
