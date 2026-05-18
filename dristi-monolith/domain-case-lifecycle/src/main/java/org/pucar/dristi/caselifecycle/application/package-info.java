/**
 * Application subdomain — interlocutory application lifecycle.
 *
 * <p>Marked as a Spring Modulith application module so cross-subdomain
 * boundaries inside {@code domain-case-lifecycle} are enforced by
 * {@code ModuleStructureTest.verify()}.
 *
 * <p>No public {@code ApplicationApi} is exposed yet — no migrated
 * subdomain calls into this one at the time of migration. An
 * {@code ApplicationApi} interface will be added in the PR of the first
 * caller that migrates.
 *
 * <p>Contract DTOs live at
 * {@code dristi-common/contract/application/} (Phase-35-lifted).
 */
@org.springframework.modulith.ApplicationModule(displayName = "Application")
package org.pucar.dristi.caselifecycle.application;
