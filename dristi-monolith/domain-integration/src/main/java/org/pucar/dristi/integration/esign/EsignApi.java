package org.pucar.dristi.integration.esign;

import org.pucar.dristi.common.contract.esign.Coordinate;
import org.pucar.dristi.common.contract.esign.CoordinateRequest;

import java.util.List;

/**
 * Public, cross-module API of the esign subdomain. Consumed today by
 * order-management for the e-sign coordinate-lookup flow; never reach
 * into {@code internal/} directly.
 *
 * <p>Contract DTOs live at {@code dristi-common/contract/esign/}.
 */
public interface EsignApi {

    /**
     * Resolve sign-placement coordinates for each criterion in the
     * request — mirrors the {@code /e-sign-svc/v1/_getLocation} REST
     * endpoint. Caller-side BSS/PDF stamping uses the returned
     * coordinates as positional placeholders.
     */
    List<Coordinate> getLocationForSign(CoordinateRequest request);
}
