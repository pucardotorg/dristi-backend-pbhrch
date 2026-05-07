/**
 * Notification subdomain's contract DTOs — request/response envelopes plus their
 * transitive payload types, lifted by Phase 35 of the per-module migration
 * pipeline. Exposed as a {@link org.springframework.modulith.NamedInterface}
 * so callers in other modules (caselifecycle, etc.) can depend on these
 * types without crossing the dristi-common boundary.
 */
@org.springframework.modulith.NamedInterface("contract-notification")
package org.pucar.dristi.common.contract.notification;
