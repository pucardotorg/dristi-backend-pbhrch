package org.pucar.dristi.caselifecycle.casemanagement.internal.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.pucar.dristi.caselifecycle.casemanagement.internal.config.Configuration;
import org.pucar.dristi.common.repository.ServiceRequestRepository;
import org.pucar.dristi.caselifecycle.casemanagement.internal.web.models.CaseCriteria;
import org.pucar.dristi.caselifecycle.casemanagement.internal.web.models.CaseSearchRequest;
import org.pucar.dristi.caselifecycle.casemanagement.internal.web.models.CourtCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

import static org.pucar.dristi.caselifecycle.casemanagement.internal.config.ServiceConstants.COURT_CASE_JSON_PATH;
import static org.pucar.dristi.caselifecycle.casemanagement.internal.config.ServiceConstants.ERROR_CASE_SEARCH;

@Slf4j
@Component("casemanagementCaseUtil")
public class CaseUtil {
    private final RestTemplate restTemplate;
    private final ObjectMapper mapper;
    private final Configuration configs;
    private final ServiceRequestRepository repository;

    @Autowired
    public CaseUtil(RestTemplate restTemplate, ObjectMapper mapper, Configuration configs, ServiceRequestRepository repository) {
        this.restTemplate = restTemplate;
        this.mapper = mapper;
        this.configs = configs;
        this.repository = repository;
    }

    public CourtCase getCase(String filingNumber, String courtId, String tenantId, Boolean isCaseFileView, RequestInfo requestInfo) {
        StringBuilder uri = new StringBuilder();
        uri.append(configs.getCaseHost()).append(configs.getCaseSearchUrl());
        CaseSearchRequest request = CaseSearchRequest.builder()
                .requestInfo(requestInfo)
                .tenantId(tenantId)
                .criteria(Collections.singletonList(CaseCriteria.builder()
                        .filingNumber(filingNumber)
                        .courtId(courtId)
                        .defaultFields(false)
                        .build()))
                .build();

        if (!Boolean.TRUE.equals(isCaseFileView)) {
            request.setFlow("flow_jac");
        }
        try {
            Object response = repository.fetchResult(uri, request);
            return mapper.convertValue(JsonPath.read(response, COURT_CASE_JSON_PATH), CourtCase.class);
        } catch (Exception e) {
            log.error("Error executing case search query", e);
            throw new CustomException("Error fetching case: ", ERROR_CASE_SEARCH);
        }
    }
}
