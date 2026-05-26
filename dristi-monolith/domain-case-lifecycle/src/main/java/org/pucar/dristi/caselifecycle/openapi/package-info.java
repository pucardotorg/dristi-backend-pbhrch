/**
 * Openapi subdomain — public-facing read-only API that aggregates case,
 * hearing, order, advocate, evidence, task, bail, and digital-document
 * data for the citizen-portal landing/search pages.
 *
 * <p>Marked as a Spring Modulith application module so cross-subdomain
 * boundaries inside {@code domain-case-lifecycle} are enforced by
 * {@code ModuleStructureTest.verify()}. No other subdomain consumes
 * openapi (it is a leaf consumer), so there is no public {@code *Api}
 * surface — the {@code @ApplicationModule} marker exists for boundary
 * enforcement (so a future caller is forced to go through a deliberate
 * API rather than reach into {@code internal/}).
 */
@org.springframework.modulith.ApplicationModule(displayName = "Openapi")
package org.pucar.dristi.caselifecycle.openapi;
