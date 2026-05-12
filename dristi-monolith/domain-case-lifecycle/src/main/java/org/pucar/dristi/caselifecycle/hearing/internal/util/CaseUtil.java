package org.pucar.dristi.caselifecycle.hearing.internal.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.tracer.model.CustomException;
import org.pucar.dristi.caselifecycle.cases.CaseApi;
import org.pucar.dristi.common.contract.hearing.CaseExistsRequest;
import org.pucar.dristi.common.contract.hearing.CaseExistsResponse;
import org.pucar.dristi.common.contract.hearing.CaseExists;
import org.pucar.dristi.common.contract.hearing.CaseCriteria;
import org.pucar.dristi.common.contract.hearing.CaseSearchRequest;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static org.pucar.dristi.caselifecycle.hearing.internal.config.ServiceConstants.ERROR_WHILE_FETCHING_FROM_CASE;

@Slf4j
@Component("hearingCaseUtil")
@AllArgsConstructor
public class CaseUtil {

    private final CaseApi caseApi;
    private final ObjectMapper mapper;

    public CaseExistsResponse fetchCaseDetails(CaseExistsRequest caseExistsRequest) {
        try {
            org.pucar.dristi.caselifecycle.cases.internal.web.models.CaseExistsRequest casesRequest =
                    new org.pucar.dristi.caselifecycle.cases.internal.web.models.CaseExistsRequest();
            casesRequest.setRequestInfo(caseExistsRequest.getRequestInfo());
            casesRequest.setCriteria(mapExistsCriteria(caseExistsRequest.getCriteria()));

            org.pucar.dristi.caselifecycle.cases.internal.web.models.CaseExistsResponse casesResponse =
                    caseApi.exists(casesRequest);

            CaseExistsResponse response = new CaseExistsResponse();
            response.setResponseInfo(casesResponse.getResponseInfo());
            response.setCriteria(unmapExistsCriteria(casesResponse.getCriteria()));
            return response;
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error(ERROR_WHILE_FETCHING_FROM_CASE, e);
            throw new CustomException(ERROR_WHILE_FETCHING_FROM_CASE, e.getMessage());
        }
    }

    public JsonNode searchCaseDetails(CaseSearchRequest caseSearchRequest) {
        try {
            org.pucar.dristi.caselifecycle.cases.internal.web.models.CaseSearchRequest casesRequest =
                    org.pucar.dristi.caselifecycle.cases.internal.web.models.CaseSearchRequest.builder()
                            .requestInfo(caseSearchRequest.getRequestInfo())
                            .criteria(mapSearchCriteria(caseSearchRequest.getCriteria()))
                            .build();
            caseApi.search(casesRequest);
            List<org.pucar.dristi.caselifecycle.cases.internal.web.models.CaseCriteria> resultCriteria =
                    casesRequest.getCriteria();
            if (resultCriteria == null || resultCriteria.isEmpty()
                    || resultCriteria.get(0).getResponseList() == null
                    || resultCriteria.get(0).getResponseList().isEmpty()) {
                throw new CustomException(ERROR_WHILE_FETCHING_FROM_CASE, "Invalid response structure from case service");
            }
            return mapper.readTree(mapper.writeValueAsString(resultCriteria.get(0).getResponseList().get(0)));
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error(ERROR_WHILE_FETCHING_FROM_CASE, e);
            throw new CustomException(ERROR_WHILE_FETCHING_FROM_CASE, e.getMessage());
        }
    }

    private List<org.pucar.dristi.caselifecycle.cases.internal.web.models.CaseExists> mapExistsCriteria(
            List<CaseExists> src) {
        List<org.pucar.dristi.caselifecycle.cases.internal.web.models.CaseExists> result = new ArrayList<>();
        if (src == null) return result;
        for (CaseExists c : src) {
            org.pucar.dristi.caselifecycle.cases.internal.web.models.CaseExists mapped =
                    new org.pucar.dristi.caselifecycle.cases.internal.web.models.CaseExists();
            mapped.setCourtCaseNumber(c.getCourtCaseNumber());
            mapped.setCnrNumber(c.getCnrNumber());
            mapped.setFilingNumber(c.getFilingNumber());
            result.add(mapped);
        }
        return result;
    }

    private List<CaseExists> unmapExistsCriteria(
            List<org.pucar.dristi.caselifecycle.cases.internal.web.models.CaseExists> src) {
        List<CaseExists> result = new ArrayList<>();
        if (src == null) return result;
        for (org.pucar.dristi.caselifecycle.cases.internal.web.models.CaseExists c : src) {
            CaseExists mapped = new CaseExists();
            mapped.setCourtCaseNumber(c.getCourtCaseNumber());
            mapped.setCnrNumber(c.getCnrNumber());
            mapped.setFilingNumber(c.getFilingNumber());
            mapped.setExists(c.getExists());
            result.add(mapped);
        }
        return result;
    }

    private List<org.pucar.dristi.caselifecycle.cases.internal.web.models.CaseCriteria> mapSearchCriteria(
            List<CaseCriteria> src) {
        List<org.pucar.dristi.caselifecycle.cases.internal.web.models.CaseCriteria> result = new ArrayList<>();
        if (src == null) return result;
        for (CaseCriteria c : src) {
            result.add(org.pucar.dristi.caselifecycle.cases.internal.web.models.CaseCriteria.builder()
                    .caseId(c.getCaseId())
                    .defaultFields(c.getDefaultFields())
                    .cnrNumber(c.getCnrNumber())
                    .filingNumber(c.getFilingNumber())
                    .outcome(c.getOutcome())
                    .courtCaseNumber(c.getCourtCaseNumber())
                    .filingFromDate(c.getFilingFromDate())
                    .filingToDate(c.getFilingToDate())
                    .registrationFromDate(c.getRegistrationFromDate())
                    .registrationToDate(c.getRegistrationToDate())
                    .judgeId(c.getJudgeId())
                    .stage(c.getStage())
                    .substage(c.getSubstage())
                    .litigantId(c.getLitigantId())
                    .advocateId(c.getAdvocateId())
                    .status(c.getStatus())
                    .build());
        }
        return result;
    }
}
