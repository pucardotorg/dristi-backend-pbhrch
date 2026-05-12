package org.pucar.dristi.payments.calculator.internal.payment.calculator.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.egov.common.contract.request.RequestInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pucar.dristi.caselifecycle.cases.CaseApi;
import org.pucar.dristi.caselifecycle.cases.internal.web.models.CaseCriteria;
import org.pucar.dristi.caselifecycle.cases.internal.web.models.CaseListResponse;
import org.pucar.dristi.caselifecycle.cases.internal.web.models.CaseSearchRequest;
import org.pucar.dristi.caselifecycle.cases.internal.web.models.CourtCase;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CaseUtilTest {

    @Mock
    private CaseApi caseApi;

    @Mock
    private ObjectMapper mapper;

    @InjectMocks
    private CaseUtil caseUtil;

    @Test
    @DisplayName("test get advocate for litigant")
    public void testGetAdvocateForLitigant() {
        RequestInfo requestInfo = new RequestInfo();
        String filingNumber = "KL-123";
        String tenantId = "pb";

        Map<String, Object> caseAsMap = new HashMap<>();
        caseAsMap.put("caseId", "123");
        caseAsMap.put("litigants", List.of(Map.of("individualId", "1")));
        caseAsMap.put("representatives", List.of(Map.of(
                "isActive", true,
                "representing", List.of(Map.of("individualId", "1", "isActive", true)))));
        JsonNode caseJson = new ObjectMapper().valueToTree(caseAsMap);

        CourtCase courtCase = CourtCase.builder().build();
        CaseCriteria criteria = CaseCriteria.builder().responseList(List.of(courtCase)).build();
        CaseListResponse response = CaseListResponse.builder().criteria(List.of(criteria)).build();

        when(caseApi.search(any(CaseSearchRequest.class))).thenReturn(response);
        when(mapper.valueToTree(any())).thenReturn(caseJson);

        Map<String, List<JsonNode>> result = caseUtil.getAdvocateForLitigant(requestInfo, filingNumber, tenantId);
        assertEquals(1, result.size());
    }
}
