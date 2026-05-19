/**
 * Application subdomain — interlocutory application lifecycle.
 *
 * <p>Marked as a Spring Modulith application module so cross-subdomain
 * boundaries inside {@code domain-case-lifecycle} are enforced by
 * {@code ModuleStructureTest.verify()}.
 *
 * <p>Other subdomains MUST consume application through
 * {@link ApplicationApi}; reaching into {@code internal/} is a
 * structural violation enforced by {@code ModuleStructureTest.verify()}.
 *
 * <p>Contract DTOs live at
 * {@code dristi-common/contract/application/} (Phase-35-lifted).
 */
@org.springframework.modulith.ApplicationModule(displayName = "Application")
@org.springframework.modulith.NamedInterface("api")
package org.pucar.dristi.caselifecycle.application;
