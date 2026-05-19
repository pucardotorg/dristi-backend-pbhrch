package org.pucar.dristi.caselifecycle.hearing.internal.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pucar.dristi.caselifecycle.hearing.internal.config.Configuration;
import org.pucar.dristi.common.contract.hearing.CaseCriteria;
import org.pucar.dristi.common.contract.hearing.CaseExists;
import org.pucar.dristi.common.contract.hearing.CaseExistsRequest;
import org.pucar.dristi.common.contract.hearing.CaseExistsResponse;
import org.pucar.dristi.common.contract.hearing.CaseSearchRequest;
import org.springframework.web.client.RestTemplate;
import org.egov.tracer.model.CustomException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CaseUtilTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ObjectMapper mapper;

    @Mock
    private Configuration configs;

    @InjectMocks
    private CaseUtil caseUtil;

    @BeforeEach
    void setUp() {
        lenient().when(configs.getCaseHost()).thenReturn("http://localhost:8080/");
        lenient().when(configs.getCaseExistsPath()).thenReturn("case/v1/_exists");
        lenient().when(configs.getCaseSearchPath()).thenReturn("case/v1/_search");
    }

    @Test
    void testFetchCaseDetails_Success() {
        CaseExistsRequest request = new CaseExistsRequest();
        CaseExists criterion = new CaseExists();
        criterion.setFilingNumber("FN-001");
        request.setCriteria(Collections.singletonList(criterion));

        Map<String, Object> restResponse = new HashMap<>();
        when(restTemplate.postForObject(anyString(), any(), eq(Map.class))).thenReturn(restResponse);

        CaseExistsResponse expected = new CaseExistsResponse();
        CaseExists returned = new CaseExists();
        returned.setFilingNumber("FN-001");
        returned.setExists(true);
        expected.setCriteria(Collections.singletonList(returned));
        when(mapper.convertValue(restResponse, CaseExistsResponse.class)).thenReturn(expected);

        CaseExistsResponse result = caseUtil.fetchCaseDetails(request);

        assertNotNull(result);
        assertFalse(result.getCriteria().isEmpty());
        assertTrue(result.getCriteria().get(0).getExists());
    }

    @Test
    void testFetchCaseDetails_Exception() {
        CaseExistsRequest request = new CaseExistsRequest();
        request.setCriteria(new ArrayList<>());

        when(restTemplate.postForObject(anyString(), any(), eq(Map.class)))
                .thenThrow(new RuntimeException("service error"));

        assertThrows(CustomException.class, () -> caseUtil.fetchCaseDetails(request));
    }

    @Test
    void testSearchCaseDetails_Success() throws Exception {
        CaseSearchRequest request = new CaseSearchRequest();
        CaseCriteria criteria = new CaseCriteria();
        criteria.setFilingNumber("FN-001");
        request.setCriteria(Collections.singletonList(criteria));

        Map<String, Object> restResponse = new HashMap<>();
        when(restTemplate.postForObject(anyString(), any(), eq(Map.class))).thenReturn(restResponse);

        ObjectMapper realMapper = new ObjectMapper();
        String serialized = "{\"criteria\":[{\"responseList\":[{\"filingNumber\":\"FN-001\"}]}]}";
        when(mapper.writeValueAsString(restResponse)).thenReturn(serialized);
        when(mapper.readTree(serialized)).thenReturn(realMapper.readTree(serialized));

        JsonNode result = caseUtil.searchCaseDetails(request);

        assertNotNull(result);
        assertEquals("FN-001", result.get("filingNumber").asText());
        verify(restTemplate).postForObject(anyString(), any(), eq(Map.class));
    }

    @Test
    void testSearchCaseDetails_Exception() {
        CaseSearchRequest request = new CaseSearchRequest();
        request.setCriteria(new ArrayList<>());

        when(restTemplate.postForObject(anyString(), any(), eq(Map.class)))
                .thenThrow(new RuntimeException("search error"));

        assertThrows(CustomException.class, () -> caseUtil.searchCaseDetails(request));
    }
}
