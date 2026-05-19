package org.pucar.dristi.caselifecycle.casemanagement.internal.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.pucar.dristi.caselifecycle.cases.CaseApi;
import org.pucar.dristi.caselifecycle.cases.internal.web.models.CaseCriteria;
import org.pucar.dristi.caselifecycle.cases.internal.web.models.CaseListResponse;
import org.pucar.dristi.caselifecycle.cases.internal.web.models.CaseSearchRequest;
import org.pucar.dristi.common.contract.casemanagement.CourtCase;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

import static org.pucar.dristi.caselifecycle.casemanagement.internal.config.ServiceConstants.ERROR_CASE_SEARCH;

@Slf4j
@Component("casemanagementCaseUtil")
public class CaseUtil {
    private final CaseApi caseApi;
    private final ObjectMapper mapper;

    public CaseUtil(CaseApi caseApi, ObjectMapper mapper) {
        this.caseApi = caseApi;
        this.mapper = mapper;
    }

    public CourtCase getCase(String filingNumber, String courtId, String tenantId, Boolean isCaseFileView, RequestInfo requestInfo) {
        CaseSearchRequest request = CaseSearchRequest.builder()
                .requestInfo(requestInfo)
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
            CaseListResponse response = caseApi.search(request);
            List<org.pucar.dristi.caselifecycle.cases.internal.web.models.CourtCase> cases =
                    response != null && response.getCriteria() != null && !response.getCriteria().isEmpty()
                            ? response.getCriteria().get(0).getResponseList()
                            : null;
            if (cases == null || cases.isEmpty()) {
                throw new CustomException("Error fetching case: ", ERROR_CASE_SEARCH);
            }
            return mapper.convertValue(cases.get(0), CourtCase.class);
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error executing case search query", e);
            throw new CustomException("Error fetching case: ", ERROR_CASE_SEARCH);
        }
    }
}
