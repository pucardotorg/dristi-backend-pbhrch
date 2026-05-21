package org.pucar.dristi.common.hearingmanagement;

import org.pucar.dristi.common.contract.hearingmanagement.InboxRequest;
import org.pucar.dristi.common.contract.hearingmanagement.InboxResponse;

/**
 * Public API of the hearingManagement subdomain. Callers (e.g. domain-integration/njdg)
 * must consume inbox queries through this interface — never by calling the
 * egov-inbox REST endpoint directly.
 *
 * <p>The current implementation wraps the external egov-inbox REST service.
 * Once the hearingmanagement subdomain is fully migrated, the implementation
 * will call internal services directly.
 */
public interface HearingManagementApi {

    InboxResponse search(InboxRequest request);
}
