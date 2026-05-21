package org.pucar.dristi.caselifecycle.application;

import org.pucar.dristi.common.contract.application.Application;
import org.pucar.dristi.common.contract.application.ApplicationSearchRequest;

import java.util.List;

/**
 * Public, cross-subdomain API of the application subdomain. Other modules
 * (casemanagement today) consume application through this interface —
 * never by importing from {@code internal/}.
 *
 * <p>Application's contract DTOs live at
 * {@code dristi-common/contract/application/} (lifted by Phase 35 during
 * the application migration). New methods here should consume those
 * types directly.
 */
public interface ApplicationApi {

    /**
     * Search applications matching the criteria in the request.
     * Returns the list of matched applications (equivalent to the
     * {@code applicationList} field of the HTTP controller's
     * {@code ApplicationListResponse}).
     */
    List<Application> search(ApplicationSearchRequest request);
}
