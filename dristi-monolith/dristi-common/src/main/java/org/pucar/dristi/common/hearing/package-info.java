/**
 * Public API of the hearing subdomain. Exposed as a {@link org.springframework.modulith.NamedInterface}
 * so other Maven modules (domain-integration, etc.) can wire against {@code HearingApi}
 * without crossing the dristi-common boundary.
 */
@org.springframework.modulith.NamedInterface("api-hearing")
package org.pucar.dristi.common.hearing;
