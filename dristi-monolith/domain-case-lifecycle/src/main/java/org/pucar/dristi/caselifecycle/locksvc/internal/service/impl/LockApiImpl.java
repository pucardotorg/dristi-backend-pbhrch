package org.pucar.dristi.caselifecycle.locksvc.internal.service.impl;

import org.egov.common.contract.request.RequestInfo;
import org.pucar.dristi.caselifecycle.locksvc.LockApi;
import org.pucar.dristi.caselifecycle.locksvc.internal.service.LockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LockApiImpl implements LockApi {

    private final LockService lockService;

    @Autowired
    public LockApiImpl(LockService lockService) {
        this.lockService = lockService;
    }

    @Override
    public boolean isLockPresent(RequestInfo requestInfo, String uniqueId, String tenantId) {
        return Boolean.TRUE.equals(lockService.isLocked(requestInfo, uniqueId, tenantId));
    }
}
