/**
 * Summons subdomain — delivery of summons / notices / warrants through
 * registered channels (e-post, ICOPS, SMS, email) tied to a task.
 *
 * <p>Marked as a Spring Modulith application module so cross-subdomain
 * boundaries inside {@code domain-integration} are enforced by
 * {@code ModuleStructureTest.verify()}. Other subdomains MUST consume
 * summons through {@link org.pucar.dristi.integration.summons.SummonsApi};
 * reaching into {@code internal/} is a structural violation.
 *
 * <p>Contract DTOs (HTTP wire format) live in
 * {@code dristi-common/contract/summons/} per Rule 24 (Phase 35 lift).
 */
@org.springframework.modulith.ApplicationModule(displayName = "Summons")
@org.springframework.modulith.NamedInterface("api")
package org.pucar.dristi.integration.summons;
