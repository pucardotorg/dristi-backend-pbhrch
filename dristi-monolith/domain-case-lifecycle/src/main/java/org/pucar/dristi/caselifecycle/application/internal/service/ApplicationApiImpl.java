package org.pucar.dristi.caselifecycle.application.internal.service;

import lombok.RequiredArgsConstructor;
import org.pucar.dristi.caselifecycle.application.ApplicationApi;
import org.pucar.dristi.common.contract.application.Application;
import org.pucar.dristi.common.contract.application.ApplicationRequest;
import org.pucar.dristi.common.contract.application.ApplicationSearchRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("applicationApiImpl")
@RequiredArgsConstructor
public class ApplicationApiImpl implements ApplicationApi {

    private final ApplicationService applicationService;

    @Override
    public List<Application> searchApplications(ApplicationSearchRequest request) {
        return applicationService.searchApplications(request);
    }

    @Override
    public Application updateApplication(ApplicationRequest request) {
        return applicationService.updateApplication(request, false);
    }
}
