/**
 * Esign subdomain — eSign integration (ESP protocol XML signing flow plus
 * digital-signature PDF stamping).
 *
 * <p>Marked as a Spring Modulith application module. No cross-subdomain
 * callers exist today (the interceptor side ports in PR 2); this marker
 * is here for consistency so that when a caller appears, the boundary
 * is already enforced.
 */
@org.springframework.modulith.ApplicationModule(displayName = "Esign")
package org.pucar.dristi.integration.esign;
