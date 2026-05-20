package org.pucar.dristi.caselifecycle.analytics.internal.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.egov.common.contract.request.RequestInfo;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pucar.dristi.caselifecycle.evidence.EvidenceApi;
import org.pucar.dristi.common.contract.evidence.Artifact;
import org.pucar.dristi.common.contract.evidence.EvidenceSearchCriteria;
import org.pucar.dristi.common.contract.evidence.EvidenceSearchResponse;
import org.pucar.dristi.common.contract.evidence.Pagination;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EvidenceUtilTest {

    @Mock
    private EvidenceApi evidenceApi;

    @Mock
    private ObjectMapper mapper;

    @InjectMocks
    private EvidenceUtil evidenceUtil;

    @Test
    void testGetEvidence_WithArtifactNumber() throws Exception {
        JSONObject request = new JSONObject().put("RequestInfo", new JSONObject());
        RequestInfo requestInfo = RequestInfo.builder().build();
        Artifact artifact = Artifact.builder().artifactNumber("ART123").build();
        EvidenceSearchResponse response = EvidenceSearchResponse.builder()
                .artifacts(List.of(artifact)).build();
        com.fasterxml.jackson.databind.node.ObjectNode artifactNode =
                new ObjectMapper().createObjectNode().put("artifactNumber", "ART123");

        when(mapper.convertValue(any(), eq(RequestInfo.class))).thenReturn(requestInfo);
        when(evidenceApi.searchEvidence(eq(requestInfo), any(EvidenceSearchCriteria.class), any(Pagination.class)))
                .thenReturn(response);
        when(mapper.valueToTree(artifact)).thenReturn(artifactNode);

        Object result = evidenceUtil.getEvidence(request, "tenant1", "ART123");
        assertNotNull(result);
        assertInstanceOf(JSONObject.class, result);
        assertEquals("ART123", ((JSONObject) result).getString("artifactNumber"));
    }

    @Test
    void testGetEvidence_NoArtifacts() throws Exception {
        JSONObject request = new JSONObject().put("RequestInfo", new JSONObject());
        RequestInfo requestInfo = RequestInfo.builder().build();
        EvidenceSearchResponse response = EvidenceSearchResponse.builder()
                .artifacts(Collections.emptyList()).build();

        when(mapper.convertValue(any(), eq(RequestInfo.class))).thenReturn(requestInfo);
        when(evidenceApi.searchEvidence(eq(requestInfo), any(EvidenceSearchCriteria.class), any(Pagination.class)))
                .thenReturn(response);

        assertNull(evidenceUtil.getEvidence(request, "tenant1", "ART999"));
    }

    @Test
    void testGetEvidence_Exception() throws Exception {
        JSONObject request = new JSONObject().put("RequestInfo", new JSONObject());
        RequestInfo requestInfo = RequestInfo.builder().build();
        when(mapper.convertValue(any(), eq(RequestInfo.class))).thenReturn(requestInfo);
        when(evidenceApi.searchEvidence(any(), any(), any())).thenThrow(new RuntimeException("upstream"));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> evidenceUtil.getEvidence(request, "tenant1", "ART123"));
        assertEquals("Error while fetching or processing the evidence response", ex.getMessage());
    }
}
