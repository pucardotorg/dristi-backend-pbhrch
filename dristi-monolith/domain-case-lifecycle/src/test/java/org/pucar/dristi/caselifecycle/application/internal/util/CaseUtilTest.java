package org.pucar.dristi.caselifecycle.application.internal.util;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.egov.tracer.model.CustomException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pucar.dristi.caselifecycle.cases.CaseApi;
import org.pucar.dristi.caselifecycle.cases.internal.web.models.CaseCriteria;
import org.pucar.dristi.caselifecycle.cases.internal.web.models.CaseExists;
import org.pucar.dristi.caselifecycle.cases.internal.web.models.CaseExistsRequest;
import org.pucar.dristi.caselifecycle.cases.internal.web.models.CaseExistsResponse;
import org.pucar.dristi.caselifecycle.cases.internal.web.models.CaseListResponse;
import org.pucar.dristi.caselifecycle.cases.internal.web.models.CaseSearchRequest;
import org.pucar.dristi.caselifecycle.cases.internal.web.models.CourtCase;

@ExtendWith(MockitoExtension.class)
public class CaseUtilTest {

    @Mock
    private CaseApi caseApi;

    @Mock
    private ObjectMapper mapper;

    @InjectMocks
    private CaseUtil caseUtil;

    @Test
    void testFetchCaseDetailsSuccess() {
        CaseExistsRequest request = new CaseExistsRequest();
        CaseExistsResponse response = CaseExistsResponse.builder()
                .criteria(List.of(CaseExists.builder().exists(true).build()))
                .build();
        when(caseApi.exists(request)).thenReturn(response);

        assertTrue(caseUtil.fetchCaseDetails(request));
    }

    @Test
    void testFetchCaseDetailsDoesNotExist() {
        CaseExistsRequest request = new CaseExistsRequest();
        CaseExistsResponse response = CaseExistsResponse.builder()
                .criteria(List.of(CaseExists.builder().exists(false).build()))
                .build();
        when(caseApi.exists(request)).thenReturn(response);

        assertFalse(caseUtil.fetchCaseDetails(request));
    }

    @Test
    void testFetchCaseDetailsException() {
        CaseExistsRequest request = new CaseExistsRequest();
        when(caseApi.exists(request)).thenThrow(new RuntimeException("Error"));

        assertThrows(CustomException.class, () -> caseUtil.fetchCaseDetails(request));
    }

    @Test
    void testSearchCaseDetails_Success() throws JsonProcessingException {
        CaseSearchRequest request = CaseSearchRequest.builder()
                .criteria(Collections.singletonList(CaseCriteria.builder().filingNumber("FN-001").build()))
                .build();

        CourtCase courtCase = new CourtCase();
        CaseCriteria criteria = new CaseCriteria();
        criteria.setResponseList(List.of(courtCase));
        CaseListResponse listResponse = CaseListResponse.builder()
                .criteria(List.of(criteria))
                .build();

        JsonNode expectedNode = new ObjectMapper().readTree("{\"caseId\":\"123\"}");
        when(caseApi.search(request)).thenReturn(listResponse);
        when(mapper.valueToTree(courtCase)).thenReturn(expectedNode);

        JsonNode result = caseUtil.searchCaseDetails(request);
        assertNotNull(result);
        assertEquals("123", result.get("caseId").asText());
    }

    @Test
    void testSearchCaseDetails_Exception() {
        CaseSearchRequest request = new CaseSearchRequest();
        when(caseApi.search(request)).thenThrow(new RuntimeException("Error fetching case"));

        assertThrows(CustomException.class, () -> caseUtil.searchCaseDetails(request));
    }

    @Test
    void testSearchCaseDetails_EmptyResponseList() {
        CaseSearchRequest request = new CaseSearchRequest();
        CaseCriteria criteria = new CaseCriteria();
        criteria.setResponseList(Collections.emptyList());
        CaseListResponse listResponse = CaseListResponse.builder()
                .criteria(List.of(criteria))
                .build();
        when(caseApi.search(request)).thenReturn(listResponse);

        assertNull(caseUtil.searchCaseDetails(request));
    }
}
