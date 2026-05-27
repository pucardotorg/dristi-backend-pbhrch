package org.pucar.dristi.caselifecycle.hearingmanagement.internal.service;

import org.pucar.dristi.caselifecycle.hearing.HearingApi;
import org.pucar.dristi.caselifecycle.hearingmanagement.internal.enrichment.HearingsEnrichment;
import org.pucar.dristi.common.contract.hearing.Hearing;
import org.pucar.dristi.common.contract.hearingmanagement.HearingCriteria;
import org.pucar.dristi.common.contract.hearingmanagement.HearingSearchListResponse;
import org.pucar.dristi.common.contract.hearingmanagement.HearingSearchRequest;
import org.pucar.dristi.common.contract.hearingmanagement.HearingSearchResponse;
import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class HearingServiceTest {

    private HearingApi hearingApi;
    private HearingsEnrichment hearingsEnrichment;
    private HearingService hearingService;

    @BeforeEach
    public void setup() {
        hearingApi = mock(HearingApi.class);
        hearingsEnrichment = mock(HearingsEnrichment.class);
        hearingService = new HearingService(hearingApi, hearingsEnrichment);
    }

    @Test
    public void testSearchHearings_successWithData() {
        HearingSearchRequest request = HearingSearchRequest.builder()
                .criteria(HearingCriteria.builder()
                        .fromDate(17980808085L)
                        .toDate(1835707057070L)
                        .courtId("court1")
                        .tenantId("tenant1")
                        .build())
                .requestInfo(RequestInfo.builder().build())
                .build();

        Hearing hearing = Hearing.builder().build();
        when(hearingApi.search(any())).thenReturn(List.of(hearing));

        List<HearingSearchResponse> enrichedList = new ArrayList<>();
        enrichedList.add(HearingSearchResponse.builder()
                .hearingDate("2025-01-01")
                .dayStatus("SomeStatus")
                .noOfHearing(1)
                .build());
        when(hearingsEnrichment.enrichHearings(anyList())).thenReturn(enrichedList);

        HearingSearchListResponse result = hearingService.searchHearings(request);

        assertEquals(1, result.getTotalCount());
        assertEquals(1, result.getHearingList().size());
    }

    @Test
    public void testSearchHearings_nullHearingList() {
        HearingSearchRequest request = HearingSearchRequest.builder().build();
        when(hearingApi.search(any())).thenReturn(null);

        HearingSearchListResponse result = hearingService.searchHearings(request);

        assertEquals(0, result.getTotalCount());
        assertTrue(result.getHearingList().isEmpty());
    }

    @Test
    public void testSearchHearings_emptyHearingList() {
        HearingSearchRequest request = HearingSearchRequest.builder()
                .criteria(HearingCriteria.builder().build())
                .requestInfo(RequestInfo.builder().build())
                .build();

        when(hearingApi.search(any())).thenReturn(new ArrayList<>());

        HearingSearchListResponse result = hearingService.searchHearings(request);

        assertEquals(0, result.getTotalCount());
        assertTrue(result.getHearingList().isEmpty());
    }

    @Test
    public void testSearchHearings_exceptionHandling() {
        HearingSearchRequest request = HearingSearchRequest.builder().build();
        when(hearingApi.search(any())).thenThrow(new RuntimeException("Failed to fetch"));

        CustomException ex = assertThrows(CustomException.class,
                () -> hearingService.searchHearings(request));
        assertEquals("HEARING_SEARCH_EXCEPTION", ex.getCode());
    }
}
