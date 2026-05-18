/**
 * Treasury subdomain's contract DTOs — payment-demand request/response
 * envelopes plus their payload types, manually lifted during the
 * treasury-backend migration (no Swagger-generated source to feed Phase 35).
 * Exposed as a {@link org.springframework.modulith.NamedInterface} so callers
 * in other modules (caselifecycle) can depend on these types when calling
 * {@code TreasuryApi}.
 */
@org.springframework.modulith.NamedInterface("contract-treasury")
package org.pucar.dristi.common.contract.treasury;
