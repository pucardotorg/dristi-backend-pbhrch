package org.pucar.dristi.caselifecycle.application.internal.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.tracer.model.CustomException;
import org.pucar.dristi.caselifecycle.cases.CaseApi;
import org.pucar.dristi.caselifecycle.cases.internal.web.models.CaseExistsRequest;
import org.pucar.dristi.caselifecycle.cases.internal.web.models.CaseExistsResponse;
import org.pucar.dristi.caselifecycle.cases.internal.web.models.CaseListResponse;
import org.pucar.dristi.caselifecycle.cases.internal.web.models.CaseSearchRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.pucar.dristi.caselifecycle.application.internal.config.ServiceConstants.*;

@Slf4j
@Component("applicationCaseUtil")
public class CaseUtil {

    private final CaseApi caseApi;
    private final ObjectMapper mapper;

    @Autowired
    public CaseUtil(CaseApi caseApi, ObjectMapper mapper) {
        this.caseApi = caseApi;
        this.mapper = mapper;
    }

    public Boolean fetchCaseDetails(CaseExistsRequest caseExistsRequest) {
        try {
            CaseExistsResponse response = caseApi.exists(caseExistsRequest);
            return response.getCriteria().get(0).getExists();
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error(ERROR_WHILE_FETCHING_FROM_CASE, e);
            throw new CustomException(ERROR_WHILE_FETCHING_FROM_CASE, e.getMessage());
        }
    }

    public JsonNode searchCaseDetails(CaseSearchRequest caseSearchRequest) {
        try {
            CaseListResponse response = caseApi.search(caseSearchRequest);
            if (response == null || response.getCriteria() == null || response.getCriteria().isEmpty()) {
                throw new CustomException(ERROR_WHILE_FETCHING_FROM_CASE, "Received null response from case search");
            }
            List<org.pucar.dristi.caselifecycle.cases.internal.web.models.CourtCase> responseList =
                    response.getCriteria().get(0).getResponseList();
            if (responseList == null || responseList.isEmpty()) {
                return null;
            }
            return mapper.valueToTree(responseList.get(0));
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error(ERROR_WHILE_FETCHING_FROM_CASE, e);
            throw new CustomException(ERROR_WHILE_FETCHING_FROM_CASE, e.getMessage());
        }
    }
}
