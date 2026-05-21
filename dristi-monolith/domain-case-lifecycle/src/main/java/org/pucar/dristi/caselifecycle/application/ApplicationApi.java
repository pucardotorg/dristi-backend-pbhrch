package org.pucar.dristi.caselifecycle.application;

import org.pucar.dristi.common.contract.application.Application;
import org.pucar.dristi.common.contract.application.ApplicationRequest;
import org.pucar.dristi.common.contract.application.ApplicationSearchRequest;

import java.util.List;

/**
 * Public, cross-subdomain API of the application subdomain. Other modules
 * (order-management today) consume application through this interface —
 * never by importing from {@code internal/}.
 *
 * <p>Application's contract DTOs live at
 * {@code dristi-common/contract/application/} (lifted by Phase 35 during
 * the application migration), so this API consumes the canonical types
 * directly.
 */
public interface ApplicationApi {

    /**
     * Search applications matching the criteria in the request.
     * Mirrors the {@code /application/v1/_search} REST endpoint.
     */
    List<Application> searchApplications(ApplicationSearchRequest request);

    /**
     * Update an existing application — mirrors {@code /application/v1/_update}.
     * The internal service has a 2-arg overload taking a
     * {@code isFromRelatedUpdate} flag; this API exposes the
     * default-{@code false} path used by REST callers.
     */
    Application updateApplication(ApplicationRequest request);
}
