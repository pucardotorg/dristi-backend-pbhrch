/**
 * Epost subdomain's contract DTOs — request/response envelopes plus their
 * transitive payload types, lifted from domain-integration. Exposed as a
 * {@link org.springframework.modulith.NamedInterface} so callers in other
 * modules can depend on these types without crossing the dristi-common boundary.
 */
@org.springframework.modulith.NamedInterface("contract-epost")
package org.pucar.dristi.common.contract.epost;
