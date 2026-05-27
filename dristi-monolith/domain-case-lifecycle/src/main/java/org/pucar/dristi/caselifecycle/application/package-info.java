/**
 * Application subdomain — interlocutory application lifecycle.
 *
 * <p>Marked as a Spring Modulith application module so cross-subdomain
 * boundaries inside {@code domain-case-lifecycle} are enforced by
 * {@code ModuleStructureTest.verify()}. Other subdomains (casemanagement,
 * order-management today) MUST consume application through
 * {@link org.pucar.dristi.caselifecycle.application.ApplicationApi};
 * reaching into {@code internal/} is a structural violation.
 *
 * <p>Contract DTOs live at
 * {@code dristi-common/contract/application/} (Phase-35-lifted during
 * the application migration).
 */
@org.springframework.modulith.ApplicationModule(displayName = "Application")
@org.springframework.modulith.NamedInterface("api")
package org.pucar.dristi.caselifecycle.application;
