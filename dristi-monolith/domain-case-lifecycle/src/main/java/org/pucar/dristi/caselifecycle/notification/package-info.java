/**
 * Notification subdomain — outbound notifications (SMS, email, etc.).
 *
 * <p>Marked as a Spring Modulith application module. No cross-subdomain
 * callers exist today; this marker is here for consistency so that when
 * a caller appears, the boundary is already enforced.
 */
@org.springframework.modulith.ApplicationModule(displayName = "Notification")
package org.pucar.dristi.caselifecycle.notification;
