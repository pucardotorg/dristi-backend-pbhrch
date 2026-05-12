/**
 * Esign contract DTOs — HTTP wire format types for the esign subdomain.
 *
 * <p>Exposed cross-subdomain via {@link org.springframework.modulith.NamedInterface}
 * so callers (PR 2's interceptor port) can wire against these types
 * without Spring Modulith flagging a "non-exposed type" violation.
 *
 * <p>Phase 35 of the per-module migration pipeline will retro-lift
 * suffix-matching DTOs (ESignRequest, ESignResponse, SignDocRequest,
 * CoordinateRequest, CoordinateResponse, StorageResponse) to
 * {@code dristi-common/contract/esign/} during C2 of this migration.
 */
@org.springframework.modulith.NamedInterface("contract")
package org.pucar.dristi.integration.esign.internal.web.models;
