/**
 * Inportalsurvey subdomain — in-portal user feedback survey
 * (eligibility check, feedback submission, remind-me-later flow).
 *
 * <p>Marked as a Spring Modulith application module. No cross-subdomain
 * callers exist today; this marker is here for consistency so that when
 * a caller appears, the boundary is already enforced.
 */
@org.springframework.modulith.ApplicationModule(displayName = "Inportalsurvey")
package org.pucar.dristi.caselifecycle.inportalsurvey;
