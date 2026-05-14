package org.pucar.dristi.caselifecycle.hearing.internal.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pucar.dristi.caselifecycle.cases.CaseApi;
import org.pucar.dristi.caselifecycle.cases.internal.web.models.CaseListResponse;
import org.pucar.dristi.common.contract.hearing.CaseCriteria;
import org.pucar.dristi.common.contract.hearing.CaseExists;
import org.pucar.dristi.common.contract.hearing.CaseExistsRequest;
import org.pucar.dristi.common.contract.hearing.CaseExistsResponse;
import org.pucar.dristi.common.contract.hearing.CaseSearchRequest;
import org.egov.tracer.model.CustomException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CaseUtilTest {

    @Mock
    private CaseApi caseApi;

    @Mock
    private ObjectMapper mapper;

    @InjectMocks
    private CaseUtil caseUtil;

    @Test
    void testFetchCaseDetails_Success() {
        CaseExistsRequest request = new CaseExistsRequest();
        CaseExists criterion = new CaseExists();
        criterion.setFilingNumber("FN-001");
        request.setCriteria(Collections.singletonList(criterion));

        org.pucar.dristi.caselifecycle.cases.internal.web.models.CaseExistsResponse casesResponse =
                new org.pucar.dristi.caselifecycle.cases.internal.web.models.CaseExistsResponse();
        org.pucar.dristi.caselifecycle.cases.internal.web.models.CaseExists casesExists =
                new org.pucar.dristi.caselifecycle.cases.internal.web.models.CaseExists();
        casesExists.setFilingNumber("FN-001");
        casesExists.setExists(true);
        casesResponse.setCriteria(Collections.singletonList(casesExists));

        when(caseApi.exists(any())).thenReturn(casesResponse);

        CaseExistsResponse result = caseUtil.fetchCaseDetails(request);

        assertNotNull(result);
        assertFalse(result.getCriteria().isEmpty());
        assertTrue(result.getCriteria().get(0).getExists());
    }

    @Test
    void testFetchCaseDetails_Exception() {
        CaseExistsRequest request = new CaseExistsRequest();
        request.setCriteria(new ArrayList<>());

        when(caseApi.exists(any())).thenThrow(new RuntimeException("service error"));

        assertThrows(CustomException.class, () -> caseUtil.fetchCaseDetails(request));
    }

    @Test
    void testSearchCaseDetails_Success() throws Exception {
        CaseSearchRequest request = new CaseSearchRequest();
        CaseCriteria criteria = new CaseCriteria();
        criteria.setFilingNumber("FN-001");
        request.setCriteria(Collections.singletonList(criteria));

        doAnswer(invocation -> {
            org.pucar.dristi.caselifecycle.cases.internal.web.models.CaseSearchRequest casesReq = invocation.getArgument(0);
            org.pucar.dristi.caselifecycle.cases.internal.web.models.CourtCase courtCase =
                    new org.pucar.dristi.caselifecycle.cases.internal.web.models.CourtCase();
            List<org.pucar.dristi.caselifecycle.cases.internal.web.models.CourtCase> responseList = Collections.singletonList(courtCase);
            casesReq.getCriteria().get(0).setResponseList(responseList);
            // Mirror CaseApi.search's contract: the request criteria
            // (now carrying responseList) is returned on the response.
            return CaseListResponse.builder().criteria(casesReq.getCriteria()).build();
        }).when(caseApi).search(any());

        ObjectMapper realMapper = new ObjectMapper();
        when(mapper.writeValueAsString(any())).thenAnswer(inv -> realMapper.writeValueAsString(inv.getArgument(0)));
        when(mapper.readTree(anyString())).thenAnswer(inv -> realMapper.readTree((String) inv.getArgument(0)));

        JsonNode result = caseUtil.searchCaseDetails(request);

        assertNotNull(result);
        verify(caseApi).search(any());
    }

    @Test
    void testSearchCaseDetails_Exception() {
        CaseSearchRequest request = new CaseSearchRequest();
        request.setCriteria(new ArrayList<>());

        when(caseApi.search(any())).thenThrow(new RuntimeException("search error"));

        assertThrows(CustomException.class, () -> caseUtil.searchCaseDetails(request));
    }
}
