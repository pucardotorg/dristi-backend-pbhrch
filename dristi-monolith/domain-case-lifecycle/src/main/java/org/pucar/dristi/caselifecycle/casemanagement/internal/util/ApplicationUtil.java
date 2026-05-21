package org.pucar.dristi.caselifecycle.casemanagement.internal.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.pucar.dristi.caselifecycle.application.ApplicationApi;
import org.pucar.dristi.common.contract.application.Application;
import org.pucar.dristi.common.contract.application.ApplicationCriteria;
import org.pucar.dristi.common.contract.application.ApplicationSearchRequest;
import org.pucar.dristi.common.contract.application.Pagination;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

import static org.pucar.dristi.caselifecycle.casemanagement.internal.config.ServiceConstants.SEARCHER_SERVICE_EXCEPTION;


@Component("casemanagementApplicationUtil")
@Slf4j
public class ApplicationUtil {

    private final ObjectMapper objectMapper;
    private final ApplicationApi applicationApi;
    private final CacheUtil cacheUtil;

    public ApplicationUtil(ObjectMapper objectMapper, ApplicationApi applicationApi, CacheUtil cacheUtil) {
        this.objectMapper = objectMapper;
        this.applicationApi = applicationApi;
        this.cacheUtil = cacheUtil;
    }

    // return list of application
    public List<org.pucar.dristi.common.contract.casemanagement.Application> searchApplications(String filingNumber, String courtId) {
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        ApplicationSearchRequest request = ApplicationSearchRequest.builder()
                .criteria(ApplicationCriteria.builder()
                        .filingNumber(filingNumber)
                        .status("PENDINGREVIEW")
                        .courtId(courtId)
                        .build())
                .build();
        try {
            List<Application> applicationList = applicationApi.search(request);
            if (applicationList == null || applicationList.isEmpty()) {
                return Collections.emptyList();
            }
            Application first = applicationList.get(0);
            cacheUtil.save(first.getTenantId() + ":" + first.getApplicationNumber(), first);
            return convertApplications(applicationList);
        } catch (Exception e) {
            log.error(SEARCHER_SERVICE_EXCEPTION, e);
            throw new CustomException(SEARCHER_SERVICE_EXCEPTION, e.getMessage());
        }
    }

    public List<org.pucar.dristi.common.contract.casemanagement.Application> searchAllApplications(String filingNumber, String courtId, String tenantId, RequestInfo requestInfo) {
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        ApplicationSearchRequest request = ApplicationSearchRequest.builder()
                .requestInfo(requestInfo)
                .criteria(ApplicationCriteria.builder()
                        .filingNumber(filingNumber)
                        .courtId(courtId)
                        .tenantId(tenantId)
                        .isHideBailCaseBundle(true)
                        .build())
                .pagination(Pagination.builder()
                        .sortBy("applicationCMPNumber")
                        .order(org.pucar.dristi.common.contract.application.Order.ASC)
                        .limit(100d)
                        .build())
                .build();
        try {
            List<Application> applicationList = applicationApi.search(request);
            return convertApplications(applicationList);
        } catch (Exception e) {
            log.error(SEARCHER_SERVICE_EXCEPTION, e);
            throw new CustomException(SEARCHER_SERVICE_EXCEPTION, e.getMessage());
        }
    }

    private List<org.pucar.dristi.common.contract.casemanagement.Application> convertApplications(List<Application> source) {
        if (source == null || source.isEmpty()) return Collections.emptyList();
        return objectMapper.convertValue(
                source,
                objectMapper.getTypeFactory().constructCollectionType(
                        List.class,
                        org.pucar.dristi.common.contract.casemanagement.Application.class));
    }
}
