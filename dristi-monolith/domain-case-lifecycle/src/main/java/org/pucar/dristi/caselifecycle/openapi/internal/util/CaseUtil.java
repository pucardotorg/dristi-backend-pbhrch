package org.pucar.dristi.caselifecycle.openapi.internal.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.pucar.dristi.caselifecycle.cases.CaseApi;
import org.pucar.dristi.caselifecycle.cases.internal.web.models.CaseListResponse;
import org.pucar.dristi.caselifecycle.cases.internal.web.models.CaseSearchRequest;
import org.pucar.dristi.caselifecycle.openapi.internal.web.models.cases.CourtCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

import static org.pucar.dristi.caselifecycle.openapi.internal.config.ServiceConstants.ERROR_CASE_SEARCH;

@Slf4j
@Component("openapiCaseUtil")
public class CaseUtil {
    private static final String FLOW_JAC = "flow_jac";

    private final CaseApi caseApi;
    private final ObjectMapper mapper;

    @Autowired
    public CaseUtil(CaseApi caseApi, ObjectMapper mapper) {
        this.caseApi = caseApi;
        this.mapper = mapper;
    }

    public CourtCase getCase(String filingNumber) {
        org.pucar.dristi.caselifecycle.cases.internal.web.models.CaseCriteria criteria =
                org.pucar.dristi.caselifecycle.cases.internal.web.models.CaseCriteria.builder()
                        .filingNumber(filingNumber)
                        .defaultFields(false)
                        .build();
        CaseSearchRequest request = CaseSearchRequest.builder()
                .requestInfo(RequestInfo.builder().build())
                .criteria(Collections.singletonList(criteria))
                .flow(FLOW_JAC)
                .build();
        try {
            CaseListResponse response = caseApi.search(request);
            List<org.pucar.dristi.caselifecycle.cases.internal.web.models.CourtCase> cases =
                    response.getCriteria().get(0).getResponseList();
            if (cases == null || cases.isEmpty()) {
                return null;
            }
            return mapper.convertValue(cases.get(0), CourtCase.class);
        } catch (Exception e) {
            log.error("Error executing case search query", e);
            throw new CustomException("Error fetching case: ", ERROR_CASE_SEARCH);
        }
    }
}
