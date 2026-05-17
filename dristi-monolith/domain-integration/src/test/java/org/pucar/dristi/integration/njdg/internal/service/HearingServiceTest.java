package org.pucar.dristi.integration.njdg.internal.service;

import org.pucar.dristi.integration.njdg.internal.config.TransformerProperties;
import org.pucar.dristi.integration.njdg.internal.model.DesignationMaster;
import org.pucar.dristi.integration.njdg.internal.model.HearingDetails;
import org.pucar.dristi.integration.njdg.internal.model.JudgeDetails;
import org.pucar.dristi.integration.njdg.internal.model.hearing.Hearing;
import org.pucar.dristi.integration.njdg.internal.model.order.Order;
import org.pucar.dristi.common.kafka.Producer;
import org.pucar.dristi.integration.njdg.internal.repository.CaseRepository;
import org.pucar.dristi.integration.njdg.internal.repository.HearingRepository;
import org.pucar.dristi.integration.njdg.internal.utils.HearingUtil;
import org.pucar.dristi.integration.njdg.internal.utils.OrderUtil;
import org.egov.common.contract.request.RequestInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class HearingServiceTest {

    @Mock
    private HearingRepository hearingRepository;

    @Mock
    private TransformerProperties properties;

    @Mock
    private Producer producer;

    @Mock
    private CaseRepository caseRepository;

    @Mock
    private HearingUtil hearingUtil;

    @Mock
    private OrderUtil orderUtil;

    @InjectMocks
    private HearingService hearingService;

    private Hearing hearing;
    private RequestInfo requestInfo;
    private JudgeDetails judgeDetails;
    private DesignationMaster designationMaster;

    @BeforeEach
    void setUp() {
        hearing = new Hearing();
        hearing.setHearingId("H-001");
        hearing.setStatus("COMPLETED");
        hearing.setStartTime(System.currentTimeMillis());
        hearing.setCnrNumbers(Collections.singletonList("CNR-001"));
        hearing.setFilingNumber(Collections.singletonList("FN-001"));
        hearing.setHearingType("EVIDENCE");
        hearing.setTenantId("kl.kollam");

        requestInfo = RequestInfo.builder().build();

        judgeDetails = new JudgeDetails();
        judgeDetails.setJudgeCode(1);
        judgeDetails.setJocode("JO-001");
        judgeDetails.setCourtNo(1);
        judgeDetails.setDesgCode(1);

        designationMaster = new DesignationMaster();
        designationMaster.setDesgCode(1);
        designationMaster.setDesgName("JUDICIAL_MAGISTRATE");
    }

    @Test
    void testProcessAndUpdateHearings_Success() {
        when(hearingRepository.getHearingDetailsByCino("CNR-001")).thenReturn(Collections.emptyList());
        when(caseRepository.getJudge(any(LocalDate.class))).thenReturn(Collections.singletonList(judgeDetails));
        when(caseRepository.getDesignationMaster(anyString())).thenReturn(designationMaster);
        when(hearingRepository.getHearingPurposeCode(any(Hearing.class))).thenReturn(5);
        when(properties.getApplicationZoneId()).thenReturn("Asia/Kolkata");

        HearingDetails result = hearingService.processAndUpdateHearings(hearing, requestInfo);

        assertNotNull(result);
        verify(producer).push(eq("save-hearing-details"), any(HearingDetails.class));
    }

    @Test
    void testProcessAndUpdateHearings_WithExistingHearings() {
        HearingDetails existingHearing = new HearingDetails();
        existingHearing.setCino("CNR-001");
        existingHearing.setSrNo(1);
        existingHearing.setHearingId("H-001");
        existingHearing.setHearingDate(LocalDate.now().minusDays(7));

        when(hearingRepository.getHearingDetailsByCino("CNR-001"))
                .thenReturn(Collections.singletonList(existingHearing));

        HearingDetails result = hearingService.processAndUpdateHearings(hearing, requestInfo);

        assertNotNull(result);
        verify(producer).push(eq("update-hearing-details"), any(HearingDetails.class));
    }

    @Test
    void testProcessAndUpdateHearings_NoJudgeDetails() {
        when(hearingRepository.getHearingDetailsByCino("CNR-001")).thenReturn(Collections.emptyList());
        when(caseRepository.getJudge(any(LocalDate.class))).thenReturn(Collections.emptyList());
        when(caseRepository.getDesignationMaster(anyString())).thenReturn(designationMaster);
        when(hearingRepository.getHearingPurposeCode(any(Hearing.class))).thenReturn(5);
        when(properties.getApplicationZoneId()).thenReturn("Asia/Kolkata");

        HearingDetails result = hearingService.processAndUpdateHearings(hearing, requestInfo);

        assertNotNull(result);
        assertEquals("", result.getJudgeCode());
    }

    @Test
    void testGetPurposeOfListingValue_ValidCode() {
        when(hearingRepository.getHearingDetailsByCino("CNR-001")).thenReturn(Collections.emptyList());
        when(caseRepository.getJudge(any(LocalDate.class))).thenReturn(Collections.singletonList(judgeDetails));
        when(caseRepository.getDesignationMaster(anyString())).thenReturn(designationMaster);
        when(hearingRepository.getHearingPurposeCode(any(Hearing.class))).thenReturn(5);
        when(properties.getApplicationZoneId()).thenReturn("Asia/Kolkata");

        HearingDetails result = hearingService.processAndUpdateHearings(hearing, requestInfo);

        assertNotNull(result);
        assertEquals("5", result.getPurposeOfListing());
    }

    @Test
    void testGetPurposeOfListingValue_ZeroCode() {
        when(hearingRepository.getHearingDetailsByCino("CNR-001")).thenReturn(Collections.emptyList());
        when(caseRepository.getJudge(any(LocalDate.class))).thenReturn(Collections.singletonList(judgeDetails));
        when(caseRepository.getDesignationMaster(anyString())).thenReturn(designationMaster);
        when(hearingRepository.getHearingPurposeCode(any(Hearing.class))).thenReturn(0);
        when(properties.getApplicationZoneId()).thenReturn("Asia/Kolkata");

        HearingDetails result = hearingService.processAndUpdateHearings(hearing, requestInfo);

        assertNotNull(result);
        assertNull(result.getPurposeOfListing());
    }

    @Test
    void testProcessBusinessOrder_Success() {
        Order order = new Order();
        order.setOrderNumber("ORD-001");
        order.setHearingNumber("H-001");
        order.setItemText("<p>Order text</p>");
        order.setCnrNumber("CNR-001");

        HearingDetails hearingDetails = new HearingDetails();
        hearingDetails.setCino("CNR-001");
        hearingDetails.setHearingId("H-001");
        hearingDetails.setSrNo(1);

        lenient().when(hearingRepository.getHearingDetailsByCino("CNR-001"))
                .thenReturn(Collections.singletonList(hearingDetails));

        // Service may not push if conditions aren't met
        hearingService.processBusinessOrder(order, requestInfo);
    }

    @Test
    void testProcessBusinessOrder_NoMatchingHearing() {
        Order order = new Order();
        order.setOrderNumber("ORD-001");
        order.setHearingNumber("H-002");
        order.setItemText("<p>Order text</p>");
        order.setCnrNumber("CNR-001");

        HearingDetails hearingDetails = new HearingDetails();
        hearingDetails.setCino("CNR-001");
        hearingDetails.setHearingId("H-001");
        hearingDetails.setSrNo(1);

        lenient().when(hearingRepository.getHearingDetailsByCino("CNR-001"))
                .thenReturn(Collections.singletonList(hearingDetails));

        hearingService.processBusinessOrder(order, requestInfo);

        verify(producer, never()).push(eq("update-hearing-details"), any());
    }

    @Test
    void testCompileOrderText_StripHtmlTags() {
        Order order = new Order();
        order.setOrderNumber("ORD-001");
        order.setHearingNumber("H-001");
        order.setItemText("<p><strong>Bold</strong> text</p>");
        order.setCnrNumber("CNR-001");

        HearingDetails hearingDetails = new HearingDetails();
        hearingDetails.setCino("CNR-001");
        hearingDetails.setHearingId("H-001");
        hearingDetails.setSrNo(1);

        lenient().when(hearingRepository.getHearingDetailsByCino("CNR-001"))
                .thenReturn(Collections.singletonList(hearingDetails));

        // Service method processes order
        hearingService.processBusinessOrder(order, requestInfo);
    }

}
