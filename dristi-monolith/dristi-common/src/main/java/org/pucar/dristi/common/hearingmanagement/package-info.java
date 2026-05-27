/**
 * Public API of the hearingManagement subdomain. Exposed as a
 * {@link org.springframework.modulith.NamedInterface} so other Maven modules
 * (domain-integration, etc.) can wire against {@code HearingManagementApi}
 * without crossing the dristi-common boundary.
 */
@org.springframework.modulith.NamedInterface("api-hearingmanagement")
package org.pucar.dristi.common.hearingmanagement;
