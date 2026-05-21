package org.pucar.dristi.caselifecycle.analytics.internal.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.egov.common.contract.request.RequestInfo;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pucar.dristi.caselifecycle.hearing.HearingApi;
import org.pucar.dristi.common.contract.hearing.Hearing;
import org.pucar.dristi.common.contract.hearing.HearingSearchRequest;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HearingUtilTest {

    @Mock
    private HearingApi hearingApi;

    @Mock
    private ObjectMapper mapper;

    @InjectMocks
    private HearingUtil hearingUtil;

    @Test
    void testGetHearing_Success() throws Exception {
        JSONObject request = new JSONObject().put("RequestInfo", new JSONObject());
        RequestInfo requestInfo = RequestInfo.builder().build();
        Hearing hearing = new Hearing();
        com.fasterxml.jackson.databind.node.ObjectNode hearingNode =
                new ObjectMapper().createObjectNode().put("id", "hearing-123");

        when(mapper.convertValue(any(), eq(RequestInfo.class))).thenReturn(requestInfo);
        when(hearingApi.search(any(HearingSearchRequest.class))).thenReturn(List.of(hearing));
        when(mapper.valueToTree(hearing)).thenReturn(hearingNode);

        Object result = hearingUtil.getHearing(request, "app-123", "cnr-123", "hearing-123", "tenant-123");
        assertNotNull(result);
        assertInstanceOf(JSONObject.class, result);
        assertEquals("hearing-123", ((JSONObject) result).getString("id"));
    }

    @Test
    void testGetHearing_NoHearings() throws Exception {
        JSONObject request = new JSONObject().put("RequestInfo", new JSONObject());
        when(mapper.convertValue(any(), eq(RequestInfo.class))).thenReturn(RequestInfo.builder().build());
        when(hearingApi.search(any(HearingSearchRequest.class))).thenReturn(Collections.emptyList());

        assertNull(hearingUtil.getHearing(request, null, null, "hearing-999", "tenant-123"));
    }

    @Test
    void testGetHearing_Exception() throws Exception {
        JSONObject request = new JSONObject().put("RequestInfo", new JSONObject());
        when(mapper.convertValue(any(), eq(RequestInfo.class))).thenReturn(RequestInfo.builder().build());
        when(hearingApi.search(any())).thenThrow(new RuntimeException("upstream"));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> hearingUtil.getHearing(request, null, null, "hearing-123", "tenant-123"));
        assertEquals("Error while fetching or processing the hearing response", ex.getMessage());
    }
}
