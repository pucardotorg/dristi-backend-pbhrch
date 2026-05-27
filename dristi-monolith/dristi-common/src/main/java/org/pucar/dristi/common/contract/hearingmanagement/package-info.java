/**
 * HearingManagement subdomain contract DTOs — inbox search request/response
 * envelopes used by the NJDG transformer to query workflow pending tasks.
 * Exposed as a {@link org.springframework.modulith.NamedInterface} so callers
 * in other modules can depend on these types without crossing the
 * dristi-common boundary.
 */
@org.springframework.modulith.NamedInterface("contract-hearingmanagement")
package org.pucar.dristi.common.contract.hearingmanagement;
