package org.pucar.dristi.caselifecycle.application.internal.service;

import org.pucar.dristi.caselifecycle.application.ApplicationApi;
import org.pucar.dristi.common.contract.application.Application;
import org.pucar.dristi.common.contract.application.ApplicationSearchRequest;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ApplicationApiImpl implements ApplicationApi {

    private final ApplicationService applicationService;

    public ApplicationApiImpl(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @Override
    public List<Application> search(ApplicationSearchRequest request) {
        return applicationService.searchApplications(request);
    }
}
