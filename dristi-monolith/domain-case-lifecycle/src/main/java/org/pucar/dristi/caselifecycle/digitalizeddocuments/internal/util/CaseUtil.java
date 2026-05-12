package org.pucar.dristi.caselifecycle.digitalizeddocuments.internal.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.pucar.dristi.caselifecycle.cases.CaseApi;
import org.pucar.dristi.caselifecycle.cases.internal.web.models.CaseCriteria;
import org.pucar.dristi.caselifecycle.cases.internal.web.models.CaseListResponse;
import org.pucar.dristi.caselifecycle.cases.internal.web.models.CaseSearchRequest;
import org.pucar.dristi.caselifecycle.cases.internal.web.models.CourtCase;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.pucar.dristi.caselifecycle.digitalizeddocuments.internal.config.ServiceConstants.ERROR_WHILE_FETCHING_FROM_CASE;

@Component("digitalizeddocumentsCaseUtil")
@Slf4j
@AllArgsConstructor
public class CaseUtil {
    private final CaseApi caseApi;
    private final ObjectMapper mapper;

    public JsonNode getCaseFromFilingNumber(RequestInfo requestInfo, String filingNumber) {
        CaseSearchRequest request = new CaseSearchRequest();
        request.setRequestInfo(requestInfo);
        request.addCriteriaItem(
                CaseCriteria.builder()
                        .filingNumber(filingNumber)
                        .defaultFields(true)
                        .build()
        );

        CaseListResponse response;
        try {
            response = caseApi.search(request);
        } catch (Exception e) {
            log.error(ERROR_WHILE_FETCHING_FROM_CASE, e);
            throw new CustomException(ERROR_WHILE_FETCHING_FROM_CASE, e.getMessage());
        }

        if (response == null || response.getCriteria() == null || response.getCriteria().isEmpty()) {
            log.error("Invalid response structure from case service");
            throw new CustomException(ERROR_WHILE_FETCHING_FROM_CASE, "Invalid response structure");
        }
        List<CourtCase> cases = response.getCriteria().get(0).getResponseList();
        if (cases == null || cases.isEmpty()) {
            log.error("No case found for filingNumber {}", filingNumber);
            throw new CustomException(ERROR_WHILE_FETCHING_FROM_CASE, "No case found");
        }
        return mapper.valueToTree(cases.get(0));
    }
}
