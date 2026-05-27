/**
 * Public API of the order subdomain. Exposed as a {@link org.springframework.modulith.NamedInterface}
 * so other Maven modules (domain-integration, etc.) can wire against {@code OrderApi}
 * without crossing the dristi-common boundary.
 */
@org.springframework.modulith.NamedInterface("api-order")
package org.pucar.dristi.common.order;
