package org.pucar.dristi.caselifecycle.ctc.internal.service;

import org.pucar.dristi.caselifecycle.ctc.CtcApi;
import org.pucar.dristi.common.contract.ctc.CtcApplication;
import org.pucar.dristi.common.contract.ctc.CtcApplicationSearchRequest;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CtcApiImpl implements CtcApi {

    private final CtcApplicationService ctcApplicationService;

    public CtcApiImpl(CtcApplicationService ctcApplicationService) {
        this.ctcApplicationService = ctcApplicationService;
    }

    @Override
    public List<CtcApplication> search(CtcApplicationSearchRequest request) {
        return ctcApplicationService.searchApplications(request);
    }
}
