package org.pucar.dristi.caselifecycle.locksvc;

import org.egov.common.contract.request.RequestInfo;

/**
 * Public, cross-subdomain API of the lock-svc subdomain. Other modules
 * (cases today; potentially others later) consume lock-svc through this
 * interface — never by importing from {@code internal/}.
 *
 * <p>Lock-svc's wire DTOs (Lock, LockRequest, LockResponse) are not
 * exposed here because the only cross-subdomain consumer needs only the
 * boolean "is this id locked?" check. New methods may be added as new
 * cross-subdomain use cases emerge; keep the surface minimal.
 */
public interface LockApi {

    /**
     * Returns {@code true} if a lock currently exists for the given
     * (uniqueId, tenantId) pair.
     *
     * @param requestInfo eGov request envelope carrying caller identity
     * @param uniqueId    application-specific identifier under contention
     * @param tenantId    tenant scope for the lock
     */
    boolean isLockPresent(RequestInfo requestInfo, String uniqueId, String tenantId);
}
