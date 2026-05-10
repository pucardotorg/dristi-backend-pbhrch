package org.pucar.dristi.caselifecycle.bailbond.internal.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.pucar.dristi.common.contract.bailbond.CaseCriteria;
import org.pucar.dristi.common.contract.bailbond.CaseSearchRequest;
import org.pucar.dristi.caselifecycle.cases.internal.service.CaseService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.tracer.model.CustomException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static org.pucar.dristi.caselifecycle.bailbond.internal.config.ServiceConstants.ERROR_WHILE_FETCHING_FROM_CASE;

@Component("bailbondCaseUtil")
@Slf4j
@AllArgsConstructor
public class CaseUtil {
    private final CaseService caseService;
    private final ObjectMapper mapper;

    public JsonNode searchCaseDetails(CaseSearchRequest caseSearchRequest) {
        try {
            org.pucar.dristi.caselifecycle.cases.internal.web.models.CaseSearchRequest casesRequest =
                    org.pucar.dristi.caselifecycle.cases.internal.web.models.CaseSearchRequest.builder()
                            .requestInfo(caseSearchRequest.getRequestInfo())
                            .criteria(mapCriteria(caseSearchRequest.getCriteria()))
                            .flow(caseSearchRequest.getFlow())
                            .build();

            caseService.searchCases(casesRequest);

            List<org.pucar.dristi.caselifecycle.cases.internal.web.models.CaseCriteria> resultCriteria =
                    casesRequest.getCriteria();
            if (resultCriteria == null || resultCriteria.isEmpty()
                    || resultCriteria.get(0).getResponseList() == null) {
                log.error("Invalid response structure from case service");
                throw new CustomException(ERROR_WHILE_FETCHING_FROM_CASE, "Invalid response structure");
            }
            return mapper.readTree(mapper.writeValueAsString(resultCriteria.get(0).getResponseList()));
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error(ERROR_WHILE_FETCHING_FROM_CASE, e);
            throw new CustomException(ERROR_WHILE_FETCHING_FROM_CASE, e.getMessage());
        }
    }

    private List<org.pucar.dristi.caselifecycle.cases.internal.web.models.CaseCriteria> mapCriteria(
            List<CaseCriteria> src) {
        List<org.pucar.dristi.caselifecycle.cases.internal.web.models.CaseCriteria> result = new ArrayList<>();
        if (src == null) return result;
        for (CaseCriteria bc : src) {
            result.add(org.pucar.dristi.caselifecycle.cases.internal.web.models.CaseCriteria.builder()
                    .caseId(bc.getCaseId())
                    .defaultFields(bc.getDefaultFields())
                    .cnrNumber(bc.getCnrNumber())
                    .filingNumber(bc.getFilingNumber())
                    .outcome(bc.getOutcome())
                    .courtCaseNumber(bc.getCourtCaseNumber())
                    .filingFromDate(bc.getFilingFromDate())
                    .filingToDate(bc.getFilingToDate())
                    .registrationFromDate(bc.getRegistrationFromDate())
                    .registrationToDate(bc.getRegistrationToDate())
                    .judgeId(bc.getJudgeId())
                    .stage(bc.getStage())
                    .substage(bc.getSubstage())
                    .litigantId(bc.getLitigantId())
                    .advocateId(bc.getAdvocateId())
                    .status(bc.getStatus())
                    .build());
        }
        return result;
    }

    private String extractFieldFromFirstCase(JsonNode caseDetails, String fieldName) {
        if (caseDetails != null && caseDetails.isArray() && !caseDetails.isEmpty()) {
            JsonNode fieldNode = caseDetails.get(0).get(fieldName);
            if (fieldNode != null && !fieldNode.isNull()) {
                return fieldNode.textValue();
            }
        }
        log.error("{} not found", fieldName);
        return null;
    }

    public String getCourtId(JsonNode caseDetails) {
        return extractFieldFromFirstCase(caseDetails, "courtId");
    }

    public String getCaseTitle(JsonNode caseDetails) {
        return extractFieldFromFirstCase(caseDetails, "caseTitle");
    }

    public String getCnrNumber(JsonNode caseDetails) {
        return extractFieldFromFirstCase(caseDetails, "cnrNumber");
    }

    public String getCaseType(JsonNode caseDetails) {
        return extractFieldFromFirstCase(caseDetails, "caseType");
    }

    public String getCourtCaseNumber(JsonNode caseDetails) { return extractFieldFromFirstCase(caseDetails, "courtCaseNumber"); }

    public String getCmpNumber(JsonNode caseDetails) { return extractFieldFromFirstCase(caseDetails, "cmpNumber"); }

    public String getCaseId(JsonNode caseDetails) { return extractFieldFromFirstCase(caseDetails, "id"); }

    public String getSubstage(JsonNode caseDetails) { return extractFieldFromFirstCase(caseDetails, "substage"); }
}
