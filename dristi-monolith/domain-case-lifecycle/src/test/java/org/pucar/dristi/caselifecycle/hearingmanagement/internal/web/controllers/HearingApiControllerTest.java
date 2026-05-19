package org.pucar.dristi.caselifecycle.hearingmanagement.internal.web.controllers;

import org.pucar.dristi.caselifecycle.hearingmanagement.internal.service.HearingService;
import org.pucar.dristi.common.contract.hearingmanagement.HearingSearchListResponse;
import org.pucar.dristi.common.contract.hearingmanagement.HearingSearchRequest;
import org.pucar.dristi.common.util.ResponseInfoFactory;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.response.ResponseInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class HearingApiControllerTest {

    @Mock
    private HearingService hearingService;

    @Mock
    private ResponseInfoFactory responseInfoFactory;

    @InjectMocks
    private HearingApiController hearingApiController;

    @Test
    public void testHearingV1SearchPost_success() {
        HearingSearchRequest request = HearingSearchRequest.builder()
                .requestInfo(RequestInfo.builder().build())
                .build();

        HearingSearchListResponse serviceResponse = HearingSearchListResponse.builder()
                .totalCount(2)
                .hearingList(new ArrayList<>())
                .build();

        when(hearingService.searchHearings(request)).thenReturn(serviceResponse);
        when(responseInfoFactory.createResponseInfoFromRequestInfo(any(), eq(true)))
                .thenReturn(ResponseInfo.builder().build());

        ResponseEntity<HearingSearchListResponse> result = hearingApiController.hearingV1SearchPost(request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(2, result.getBody().getTotalCount());
    }
}
