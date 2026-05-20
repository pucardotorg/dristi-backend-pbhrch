package org.pucar.dristi.caselifecycle.analytics.internal.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.egov.common.contract.request.RequestInfo;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pucar.dristi.caselifecycle.analytics.internal.config.Configuration;
import org.pucar.dristi.caselifecycle.cases.CaseApi;
import org.pucar.dristi.caselifecycle.cases.internal.web.models.CaseCriteria;
import org.pucar.dristi.caselifecycle.cases.internal.web.models.CaseListResponse;
import org.pucar.dristi.caselifecycle.cases.internal.web.models.CaseSearchRequest;
import org.pucar.dristi.caselifecycle.cases.internal.web.models.CourtCase;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CaseUtilTest {

    @Mock
    private Configuration config;

    @Mock
    private CaseApi caseApi;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ObjectMapper mapper;

    @InjectMocks
    private CaseUtil caseUtil;

    @Test
    void testGetCase_FoundCase() throws Exception {
        JSONObject request = new JSONObject().put("RequestInfo", new JSONObject());
        RequestInfo requestInfo = RequestInfo.builder().build();
        CourtCase courtCase = new CourtCase();
        CaseCriteria criteria = new CaseCriteria();
        criteria.setResponseList(List.of(courtCase));
        CaseListResponse response = new CaseListResponse();
        response.setCriteria(List.of(criteria));
        com.fasterxml.jackson.databind.node.ObjectNode caseNode =
                new ObjectMapper().createObjectNode().put("caseId", "CASE123");

        when(mapper.convertValue(any(), eq(RequestInfo.class))).thenReturn(requestInfo);
        when(caseApi.search(any(CaseSearchRequest.class))).thenReturn(response);
        when(mapper.valueToTree(courtCase)).thenReturn(caseNode);

        Object result = caseUtil.getCase(request, "tenant1", "CNR123", "FIL123", "CASE123");
        assertNotNull(result);
        assertInstanceOf(JSONObject.class, result);
        assertEquals("CASE123", ((JSONObject) result).getString("caseId"));
    }

    @Test
    void testGetCase_NoCases() throws Exception {
        JSONObject request = new JSONObject().put("RequestInfo", new JSONObject());
        CaseCriteria criteria = new CaseCriteria();
        criteria.setResponseList(Collections.emptyList());
        CaseListResponse response = new CaseListResponse();
        response.setCriteria(List.of(criteria));

        when(mapper.convertValue(any(), eq(RequestInfo.class))).thenReturn(RequestInfo.builder().build());
        when(caseApi.search(any(CaseSearchRequest.class))).thenReturn(response);

        assertNull(caseUtil.getCase(request, "tenant1", "CNR999", null, null));
    }

    @Test
    void testGetCase_Exception() throws Exception {
        JSONObject request = new JSONObject().put("RequestInfo", new JSONObject());
        when(mapper.convertValue(any(), eq(RequestInfo.class))).thenReturn(RequestInfo.builder().build());
        when(caseApi.search(any())).thenThrow(new RuntimeException("upstream"));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> caseUtil.getCase(request, "tenant1", "CNR123", null, null));
        assertEquals("Error while processing case response", ex.getMessage());
    }
}
