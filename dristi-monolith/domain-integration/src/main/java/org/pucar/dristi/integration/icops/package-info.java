/**
 * Icops subdomain — Kerala iCops police-process integration.
 *
 * <p>Marked as a Spring Modulith application module so cross-subdomain
 * boundaries inside {@code domain-integration} are enforced by
 * {@code ModuleStructureTest}. Other subdomains (summons today) MUST
 * consume icops through {@link IcopsApi}; reaching into {@code internal/}
 * is a structural violation. The top-level package is exposed via
 * {@code @NamedInterface("api")}.
 */
@org.springframework.modulith.ApplicationModule(displayName = "Icops")
@org.springframework.modulith.NamedInterface("api")
package org.pucar.dristi.integration.icops;
