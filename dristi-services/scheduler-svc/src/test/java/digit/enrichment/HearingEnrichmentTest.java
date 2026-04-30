package digit.enrichment;

import digit.config.Configuration;
import digit.web.models.AuditDetails;
import digit.repository.HearingRepository;
import digit.util.DateUtil;
import digit.web.models.MdmsHearing;
import digit.web.models.MdmsSlot;
import digit.web.models.ScheduleHearing;
import digit.web.models.ScheduleHearingRequest;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.when;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;

@ExtendWith(MockitoExtension.class)
public class HearingEnrichmentTest {

    @InjectMocks
    private HearingEnrichment hearingEnrichment;


    @Mock
    private HearingRepository repository;

    @Mock
    private Configuration configuration;

    @Mock
    private DateUtil dateUtil;

    @BeforeEach
    void setUp() {
        lenient().when(configuration.getZoneId()).thenReturn("Asia/Kolkata");
        lenient().when(dateUtil.getCurrentOffsetDateTime()).thenReturn(OffsetDateTime.now(ZoneOffset.UTC));
    }

    @Test
    void testEnrichScheduleHearing() {
        RequestInfo requestInfo = new RequestInfo();
        User user = new User();
        user.setUuid("test-uuid");
        requestInfo.setUserInfo(user);

        ScheduleHearing hearing1 = new ScheduleHearing();
        hearing1.setTenantId("tenantId1");
        hearing1.setHearingBookingId("hearingId1");
        hearing1.setHearingDate(OffsetDateTime.now(ZoneOffset.UTC));
        hearing1.setJudgeId("judge1");
        hearing1.setHearingType("ADMISSION");
        hearing1.setStartTime(OffsetDateTime.now(ZoneOffset.UTC));

        ScheduleHearing hearing2 = new ScheduleHearing();
        hearing2.setTenantId("tenantId1");
        hearing2.setHearingBookingId("hearingId2");
        hearing2.setHearingDate(OffsetDateTime.now(ZoneOffset.UTC));
        hearing2.setJudgeId("judge1");
        hearing2.setHearingType("ADMISSION");
        hearing2.setStartTime(OffsetDateTime.now(ZoneOffset.UTC));

        List<ScheduleHearing> hearingList = Arrays.asList(hearing1, hearing2);

        ScheduleHearingRequest schedulingRequests = new ScheduleHearingRequest();
        schedulingRequests.setRequestInfo(requestInfo);
        schedulingRequests.setHearing(hearingList);

        List<MdmsSlot> defaultSlots = new ArrayList<>();
        Map<String, MdmsHearing> hearingTypeMap = new HashMap<>();
        MdmsHearing mdmsHearing = new MdmsHearing();
        mdmsHearing.setHearingTime(30);
        hearingTypeMap.put("ADMISSION", mdmsHearing);

        hearingEnrichment.enrichScheduleHearing(schedulingRequests, defaultSlots, hearingTypeMap);

        assertNotNull(hearing1.getAuditDetails());
        assertNotNull(hearing2.getAuditDetails());
        assertEquals("hearingId1", hearing1.getHearingBookingId());
        assertEquals("hearingId2", hearing2.getHearingBookingId());
        assertEquals(1, hearing1.getRowVersion());
        assertEquals(1, hearing2.getRowVersion());
    }

    @Test
    void testUpdateTimingInHearings() {
        ScheduleHearing hearing1 = new ScheduleHearing();
        hearing1.setHearingDate(OffsetDateTime.now(ZoneOffset.UTC));
        hearing1.setJudgeId("judge1");
        hearing1.setHearingType("ADMISSION");
        hearing1.setStartTime(OffsetDateTime.now(ZoneOffset.UTC));
        hearing1.setEndTime(OffsetDateTime.now(ZoneOffset.UTC));

        List<ScheduleHearing> hearingList = Collections.singletonList(hearing1);

        List<MdmsSlot> defaultSlots = new ArrayList<>();
        MdmsSlot slot = new MdmsSlot();
        slot.setSlotStartTime("09:00:00");
        slot.setSlotEndTime("17:00:00");
        defaultSlots.add(slot);

        Map<String, MdmsHearing> hearingTypeMap = new HashMap<>();
        MdmsHearing mdmsHearing = new MdmsHearing();
        mdmsHearing.setHearingTime(30);
        hearingTypeMap.put("ADMISSION", mdmsHearing);

        when(repository.getHearings(any(), any(), any())).thenReturn(new ArrayList<>());
        when(dateUtil.getLocalTime(anyString())).thenReturn(LocalTime.of(10, 0));
        when(dateUtil.getLocalDateFromOffsetDateTime(any())).thenReturn(LocalDate.now());

        hearingEnrichment.updateTimingInHearings(hearingList, hearingTypeMap, defaultSlots);

    }

    @Test
    void testEnrichUpdateScheduleHearing() {
        RequestInfo requestInfo = new RequestInfo();
        User user = new User();
        user.setUuid("test-uuid");
        requestInfo.setUserInfo(user);

        ScheduleHearing hearing1 = new ScheduleHearing();
        AuditDetails auditDetails = new AuditDetails();
        hearing1.setAuditDetails(auditDetails);
        hearing1.setRowVersion(1);

        List<ScheduleHearing> hearingList = Collections.singletonList(hearing1);

        hearingEnrichment.enrichUpdateScheduleHearing(requestInfo, hearingList);

        assertEquals(2, hearing1.getRowVersion());
        assertEquals("test-uuid", hearing1.getAuditDetails().getLastModifiedBy());
        assertNotNull(hearing1.getAuditDetails().getLastModifiedTime());
    }

    @Test
    void testUpdateHearingTime() {
        ScheduleHearing hearing = new ScheduleHearing();
        hearing.setHearingDate(OffsetDateTime.now(ZoneOffset.UTC));
        hearing.setHearingType("ADMISSION");
        hearing.setStartTime(OffsetDateTime.now(ZoneOffset.UTC));
        hearing.setEndTime(OffsetDateTime.now(ZoneOffset.UTC));

        List<MdmsSlot> slots = new ArrayList<>();
        MdmsSlot slot = new MdmsSlot();
        slot.setSlotStartTime("09:00:00");
        slot.setSlotEndTime("17:00:00");
        slots.add(slot);

        List<ScheduleHearing> scheduledHearings = new ArrayList<>();

        when(dateUtil.getLocalTime(anyString())).thenReturn(LocalTime.of(10, 0));
        when(dateUtil.getLocalDateFromOffsetDateTime(any())).thenReturn(LocalDate.now());
        hearingEnrichment.updateHearingTime(hearing, slots, scheduledHearings, 30);

        assertNotNull(hearing.getStartTime());
        assertNotNull(hearing.getEndTime());
    }

    @Test
    void testCanScheduleHearings() {
        ScheduleHearing hearing1 = new ScheduleHearing();
        hearing1.setStartTime(OffsetDateTime.of(LocalDate.now(), LocalTime.of(10, 0), ZoneOffset.UTC));
        hearing1.setEndTime(OffsetDateTime.of(LocalDate.now(), LocalTime.of(10, 30), ZoneOffset.UTC));

        ScheduleHearing hearing2 = new ScheduleHearing();
        hearing2.setStartTime(OffsetDateTime.of(LocalDate.now(), LocalTime.of(11, 0), ZoneOffset.UTC));
        hearing2.setEndTime(OffsetDateTime.of(LocalDate.now(), LocalTime.of(11, 30), ZoneOffset.UTC));

        List<ScheduleHearing> scheduledHearings = Collections.singletonList(hearing2);

        List<MdmsSlot> slots = new ArrayList<>();
        MdmsSlot slot = new MdmsSlot();
        slot.setSlotStartTime("09:00:00");
        slot.setSlotEndTime("17:00:00");
        slots.add(slot);

        when(dateUtil.getLocalTime(anyString())).thenReturn(LocalTime.of(9, 0), LocalTime.of(17, 0));
        when(dateUtil.getLocalDateFromOffsetDateTime(any())).thenReturn(LocalDate.now());

        boolean canSchedule = hearingEnrichment.canScheduleHearings(hearing1, scheduledHearings, slots);

        // hearing1 (10:00-10:30) and hearing2 (11:00-11:30) don't overlap, so hearing1 can be scheduled
        assertTrue(canSchedule);
    }

    @Test
    void testEnrichBulkReschedule() {
        RequestInfo requestInfo = new RequestInfo();
        User user = new User();
        user.setUuid("test-uuid");
        requestInfo.setUserInfo(user);

        ScheduleHearing hearing1 = new ScheduleHearing();
        AuditDetails auditDetails = new AuditDetails();
        hearing1.setAuditDetails(auditDetails);
        hearing1.setRowVersion(1);
        hearing1.setHearingDate(OffsetDateTime.now(ZoneOffset.UTC));
        hearing1.setJudgeId("judge1");
        hearing1.setHearingType("ADMISSION");
        hearing1.setStartTime(OffsetDateTime.now(ZoneOffset.UTC));
        hearing1.setEndTime(OffsetDateTime.now(ZoneOffset.UTC));


        List<ScheduleHearing> hearingList = Collections.singletonList(hearing1);

        ScheduleHearingRequest request = new ScheduleHearingRequest();
        request.setRequestInfo(requestInfo);
        request.setHearing(hearingList);

        List<MdmsSlot> defaultSlots = new ArrayList<>();
        MdmsSlot slot = new MdmsSlot();
        slot.setSlotStartTime("09:00:00");
        slot.setSlotEndTime("17:00:00");
        defaultSlots.add(slot);

        Map<String, MdmsHearing> hearingTypeMap = new HashMap<>();
        MdmsHearing mdmsHearing = new MdmsHearing();
        mdmsHearing.setHearingTime(30);
        hearingTypeMap.put("ADMISSION", mdmsHearing);

        when(repository.getHearings(any(), any(), any())).thenReturn(new ArrayList<>());
        when(dateUtil.getLocalTime(anyString())).thenReturn(LocalTime.of(10, 0));
        when(dateUtil.getLocalDateFromOffsetDateTime(any())).thenReturn(LocalDate.now());

        hearingEnrichment.enrichBulkReschedule(request, defaultSlots, hearingTypeMap);

        assertEquals(2, hearing1.getRowVersion());
        assertEquals("test-uuid", hearing1.getAuditDetails().getLastModifiedBy());
        assertNotNull(hearing1.getAuditDetails().getLastModifiedTime());
        assertNotNull(hearing1.getStartTime());
        assertNotNull(hearing1.getEndTime());
    }
}

