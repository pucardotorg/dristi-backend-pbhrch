package org.pucar.dristi.caselifecycle.ordermanagement.internal.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.pucar.dristi.caselifecycle.application.ApplicationApi;
import org.pucar.dristi.caselifecycle.ordermanagement.internal.web.models.application.Application;
import org.pucar.dristi.caselifecycle.ordermanagement.internal.web.models.application.ApplicationRequest;
import org.pucar.dristi.caselifecycle.ordermanagement.internal.web.models.application.ApplicationResponse;
import org.pucar.dristi.caselifecycle.ordermanagement.internal.web.models.application.ApplicationSearchRequest;

import java.util.Collections;
import java.util.List;

import static org.pucar.dristi.caselifecycle.ordermanagement.internal.config.ServiceConstants.SEARCHER_SERVICE_EXCEPTION;

@Component("ordermanagementApplicationUtil")
@Slf4j
public class ApplicationUtil {

    private final ObjectMapper objectMapper;
    private final ApplicationApi applicationApi;
    private final CacheUtil cacheUtil;

    @Autowired
    public ApplicationUtil(ObjectMapper objectMapper, ApplicationApi applicationApi, CacheUtil cacheUtil) {
        this.objectMapper = objectMapper;
        this.applicationApi = applicationApi;
        this.cacheUtil = cacheUtil;
    }

    public List<Application> searchApplications(ApplicationSearchRequest request) {
        try {
            Object redisResponse = cacheUtil.findById(
                    request.getCriteria().getTenantId() + ":" + request.getCriteria().getApplicationNumber());
            if (redisResponse != null) {
                Application application = objectMapper.readValue(
                        objectMapper.writeValueAsString(redisResponse), Application.class);
                return List.of(application);
            }
            org.pucar.dristi.common.contract.application.ApplicationSearchRequest bridgedRequest =
                    objectMapper.convertValue(request,
                            org.pucar.dristi.common.contract.application.ApplicationSearchRequest.class);
            List<org.pucar.dristi.common.contract.application.Application> apiResult =
                    applicationApi.searchApplications(bridgedRequest);
            if (apiResult == null || apiResult.isEmpty()) {
                return Collections.emptyList();
            }
            List<Application> applications = apiResult.stream()
                    .map(a -> objectMapper.convertValue(a, Application.class))
                    .toList();
            Application first = applications.get(0);
            cacheUtil.save(first.getTenantId() + ":" + first.getApplicationNumber(), first);
            return applications;
        } catch (Exception e) {
            log.error(SEARCHER_SERVICE_EXCEPTION, e);
            throw new CustomException(SEARCHER_SERVICE_EXCEPTION, e.getMessage());
        }
    }

    public ApplicationResponse updateApplication(ApplicationRequest request) {
        try {
            org.pucar.dristi.common.contract.application.ApplicationRequest bridgedRequest =
                    objectMapper.convertValue(request,
                            org.pucar.dristi.common.contract.application.ApplicationRequest.class);
            org.pucar.dristi.common.contract.application.Application apiResult =
                    applicationApi.updateApplication(bridgedRequest);
            if (apiResult != null) {
                Application updated = objectMapper.convertValue(apiResult, Application.class);
                cacheUtil.save(updated.getTenantId() + ":" + updated.getApplicationNumber(), updated);
                return ApplicationResponse.builder()
                        .application(objectMapper.valueToTree(updated))
                        .build();
            }
            return ApplicationResponse.builder().build();
        } catch (Exception e) {
            log.error(SEARCHER_SERVICE_EXCEPTION, e);
            throw new CustomException(SEARCHER_SERVICE_EXCEPTION, e.getMessage());
        }
    }
}
