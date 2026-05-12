package org.pucar.dristi.caselifecycle.bailbond.internal.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.pucar.dristi.caselifecycle.cases.CaseApi;
import org.pucar.dristi.caselifecycle.cases.internal.web.models.CourtCase;
import org.pucar.dristi.common.contract.bailbond.CaseCriteria;
import org.pucar.dristi.common.contract.bailbond.CaseSearchRequest;
import org.egov.tracer.model.CustomException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.pucar.dristi.caselifecycle.bailbond.internal.config.ServiceConstants.ERROR_WHILE_FETCHING_FROM_CASE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class CaseUtilTest {

    private CaseApi caseApi;
    private ObjectMapper objectMapper;
    private CaseUtil caseUtil;

    @BeforeEach
    void setup() {
        caseApi = mock(CaseApi.class);
        objectMapper = new ObjectMapper();
        caseUtil = new CaseUtil(caseApi, objectMapper);
    }

    @Test
    void testSearchCaseDetailsSuccess() throws Exception {
        CourtCase courtCase = new CourtCase();
        courtCase.setCourtId("COURT-123");

        doAnswer(invocation -> {
            org.pucar.dristi.caselifecycle.cases.internal.web.models.CaseSearchRequest req =
                    invocation.getArgument(0);
            req.getCriteria().get(0).setResponseList(Collections.singletonList(courtCase));
            return null;
        }).when(caseApi).search(any());

        CaseSearchRequest request = CaseSearchRequest.builder()
                .criteria(List.of(CaseCriteria.builder().filingNumber("FN-001").defaultFields(true).build()))
                .build();

        JsonNode result = caseUtil.searchCaseDetails(request);

        assertNotNull(result);
        assertEquals("COURT-123", result.get(0).get("courtId").asText());
    }

    @Test
    void testSearchCaseDetails_InvalidStructure_ThrowsCustomException() {
        // caseApi.search not stubbed — responseList stays null
        CaseSearchRequest request = CaseSearchRequest.builder()
                .criteria(List.of(CaseCriteria.builder().build()))
                .build();

        CustomException ex = assertThrows(CustomException.class,
                () -> caseUtil.searchCaseDetails(request));

        assertEquals(ERROR_WHILE_FETCHING_FROM_CASE, ex.getCode());
    }

    @Test
    void testSearchCaseDetails_ExceptionDuringProcessing_ThrowsCustomException() {
        doThrow(new RuntimeException("Service down")).when(caseApi).search(any());

        CaseSearchRequest request = CaseSearchRequest.builder()
                .criteria(List.of(CaseCriteria.builder().build()))
                .build();

        CustomException ex = assertThrows(CustomException.class,
                () -> caseUtil.searchCaseDetails(request));

        assertEquals(ERROR_WHILE_FETCHING_FROM_CASE, ex.getCode());
        assertTrue(ex.getMessage().contains("Service down"));
    }

    @Test
    void testExtractFieldsSuccess() {
        ObjectNode caseNode = objectMapper.createObjectNode();
        caseNode.put("courtId", "C1");
        caseNode.put("caseTitle", "ABC vs XYZ");
        caseNode.put("cnrNumber", "CNR123");
        caseNode.put("caseType", "CMP");
        caseNode.put("courtCaseNumber", "CCN-456");
        caseNode.put("cmpNumber", "CMP789");
        caseNode.put("id", "CASE123");

        ArrayNode arrayNode = objectMapper.createArrayNode();
        arrayNode.add(caseNode);

        assertEquals("C1", caseUtil.getCourtId(arrayNode));
        assertEquals("ABC vs XYZ", caseUtil.getCaseTitle(arrayNode));
        assertEquals("CNR123", caseUtil.getCnrNumber(arrayNode));
        assertEquals("CMP", caseUtil.getCaseType(arrayNode));
        assertEquals("CCN-456", caseUtil.getCourtCaseNumber(arrayNode));
        assertEquals("CMP789", caseUtil.getCmpNumber(arrayNode));
        assertEquals("CASE123", caseUtil.getCaseId(arrayNode));
    }

    @Test
    void testExtractField_NullOrEmpty_ShouldReturnNull() {
        ArrayNode emptyArray = objectMapper.createArrayNode();
        assertNull(caseUtil.getCaseTitle(emptyArray));

        ArrayNode arrayNode = objectMapper.createArrayNode();
        arrayNode.add(objectMapper.createObjectNode());
        assertNull(caseUtil.getCourtId(arrayNode));
    }
}
