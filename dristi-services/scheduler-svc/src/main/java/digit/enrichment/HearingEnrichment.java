package digit.enrichment;


import digit.config.Configuration;
import digit.repository.HearingRepository;
import digit.util.DateUtil;
import digit.web.models.*;
import digit.web.models.AuditDetails;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;

@Component
@Slf4j
public class HearingEnrichment {

    private final HearingRepository repository;
    private final DateUtil dateUtil;
    private final Configuration configuration;

    @Autowired
    public HearingEnrichment(HearingRepository repository, DateUtil dateUtil, Configuration configuration) {
        this.repository = repository;
        this.dateUtil = dateUtil;
        this.configuration = configuration;
    }


    public void enrichScheduleHearing(ScheduleHearingRequest schedulingRequests, List<MdmsSlot> defaultSlots, Map<String, MdmsHearing> hearingTypeMap) {
        log.info("operation = enrichScheduleHearing , result=IN_PROGRESS ");
        RequestInfo requestInfo = schedulingRequests.getRequestInfo();
        List<ScheduleHearing> hearingList = schedulingRequests.getHearing();

        AuditDetails auditDetails = getAuditDetailsScheduleHearing(requestInfo);
        for (ScheduleHearing hearing : hearingList) {
            hearing.setAuditDetails(auditDetails);
            hearing.setRowVersion(1);
            if ("BLOCKED".equals(hearing.getStatus())) {
                hearing.setHearingBookingId(UUID.randomUUID().toString());
            }
        }

        updateTimingInHearings(hearingList, hearingTypeMap, defaultSlots);
        log.info("operation = enrichScheduleHearing, result=SUCCESS");
    }


    void updateTimingInHearings(List<ScheduleHearing> hearingList, Map<String, MdmsHearing> hearingTypeMap, List<MdmsSlot> defaultSlots) {
        log.info("operation = updateTimingInHearings , result=IN_PROGRESS");

        List<String> statuses = List.of("SCHEDULED", "BLOCKED");

        HashMap<String, List<ScheduleHearing>> sameDayHearings = new HashMap<>();
        for (ScheduleHearing hearing : hearingList) {

            List<ScheduleHearing> hearings;

            String key = hearing.getJudgeId() + "_" + hearing.getStartTime() + "_" + hearing.getEndTime();
            if (sameDayHearings.containsKey(key)) {
                hearings = sameDayHearings.get(key);
            } else {
                ScheduleHearingSearchCriteria searchCriteria = ScheduleHearingSearchCriteria.builder()
                        .judgeId(hearing.getJudgeId())
                        .startDateTime(hearing.getStartTime())
                        .endDateTime(hearing.getEndTime())
                        .status(statuses).build();

                hearings = repository.getHearings(searchCriteria, null, null);
                sameDayHearings.put(key, hearings);
            }

            Integer hearingTime = hearingTypeMap.get(hearing.getHearingType()).getHearingTime();
            updateHearingTime(hearing, defaultSlots, hearings, hearingTime);
            sameDayHearings.get(key).add(hearing);
        }

        log.info("operation = updateTimingInHearings, result=SUCCESS");

    }


    public void enrichUpdateScheduleHearing(RequestInfo requestInfo, List<ScheduleHearing> hearingList) {
        log.info("operation = enrichUpdateScheduleHearing , Result = IN_PROGRESS");

        hearingList.forEach((hearing) -> {
            hearing.getAuditDetails().setLastModifiedTime(dateUtil.getCurrentOffsetDateTime());
            hearing.getAuditDetails().setLastModifiedBy(requestInfo.getUserInfo().getUuid());
            hearing.setRowVersion(hearing.getRowVersion() + 1);
        });
        log.info("operation = enrichUpdateScheduleHearing, Result=SUCCESS");
    }

    private AuditDetails getAuditDetailsScheduleHearing(RequestInfo requestInfo) {
        OffsetDateTime now = dateUtil.getCurrentOffsetDateTime();
        return AuditDetails.builder()
                .createdBy(requestInfo.getUserInfo().getUuid())
                .createdTime(now)
                .lastModifiedBy(requestInfo.getUserInfo().getUuid())
                .lastModifiedTime(now)
                .build();
    }

    void updateHearingTime(ScheduleHearing hearing, List<MdmsSlot> slots, List<ScheduleHearing> scheduledHearings, int hearingDuration) {
        log.info("operation = updateHearingTime, Result= IN_PROGRESS, hearingId:{}", hearing.getHearingBookingId());

        LocalDate date = dateUtil.getLocalDateFromOffsetDateTime(hearing.getStartTime() != null ? hearing.getStartTime() : dateUtil.getCurrentOffsetDateTime());

        for (MdmsSlot slot : slots) {
            LocalTime currentStartTime = dateUtil.getLocalTime(slot.getSlotStartTime());

            boolean flag = true;
            while (!currentStartTime.isAfter(dateUtil.getLocalTime(slot.getSlotEndTime()))) {
                LocalTime currentEndTime = currentStartTime.plusMinutes(hearingDuration);
                OffsetDateTime startDateTime = LocalDateTime.of(date, currentStartTime).atZone(java.time.ZoneId.of(configuration.getZoneId())).toOffsetDateTime();
                OffsetDateTime endDateTime = LocalDateTime.of(date, currentEndTime).atZone(java.time.ZoneId.of(configuration.getZoneId())).toOffsetDateTime();
                hearing.setStartTime(startDateTime);
                hearing.setEndTime(endDateTime);

                if (canScheduleHearings(hearing, scheduledHearings, slots)) {
                    // Hearing scheduled successfully
                    flag = false;
                    break;
                }
                currentStartTime = currentStartTime.plusMinutes(1); // Move to the next time slot
            }
            if (!flag) break;
        }
    }


    boolean canScheduleHearings(ScheduleHearing newHearing, List<ScheduleHearing> scheduledHearings, List<MdmsSlot> slots) {
        log.info("operation = canScheduleHearings , Result=IN_PROGRESS");
        // Check if new Hearings overlaps with existing Hearings and fits within any of the slots
        for (ScheduleHearing hearing : scheduledHearings) {
            if (newHearing.overlapsWith(hearing)) {
                log.debug("slot overlaps with existing hearing");
                log.info("operation = canScheduleHearings , Result=SUCCESS");
                return false;
            }

        }

        //todo : here we need to check only one slot no need to check all the slot
        for (MdmsSlot slot : slots) {

            // Compare using OffsetDateTime
            OffsetDateTime hearingEndTime = newHearing.getEndTime();
            OffsetDateTime hearingStartTime = newHearing.getStartTime();
            if (hearingEndTime == null || hearingStartTime == null) {
                continue;
            }
            LocalDate hearingDate = dateUtil.getLocalDateFromOffsetDateTime(hearingStartTime);
            LocalDateTime slotStart = LocalDateTime.of(hearingDate, dateUtil.getLocalTime(slot.getSlotStartTime()));
            LocalDateTime slotEnd = LocalDateTime.of(hearingDate, dateUtil.getLocalTime(slot.getSlotEndTime()));
            LocalDateTime hearingEndLocal = hearingEndTime.toLocalDateTime();

            if (hearingEndLocal.isAfter(slotStart) && hearingEndLocal.isBefore(slotEnd)) {
                log.debug("found slot for hearing, slotId:{}, hearingId:{}", slot.getId(), newHearing.getHearingType());
                log.info("operation = canScheduleHearings , Result=SUCCESS, slot found for hearing");
                return true;
            }
        }
        log.debug("hearing does not fit in the slot");
        log.info("operation = canScheduleHearings , Result=SUCCESS");
        return false;
    }


    public void enrichBulkReschedule(ScheduleHearingRequest request, List<MdmsSlot> defaultHearings, Map<String, MdmsHearing> hearingTypeMap) {

        log.info("operation = enrichBulkReschedule, result=IN_PROGRESS");
        List<ScheduleHearing> hearing = request.getHearing();


        String uuid = request.getRequestInfo().getUserInfo().getUuid();
        Long currentTime = System.currentTimeMillis();
        hearing.forEach((element) -> {

            element.getAuditDetails().setLastModifiedTime(dateUtil.getCurrentOffsetDateTime());
            element.getAuditDetails().setLastModifiedBy(uuid);
            element.setRowVersion(element.getRowVersion() + 1);
            element.setExpiryTime(element.getAuditDetails().getLastModifiedTime().plusNanos(configuration.getExpiryIntervalMiliSeconds() * 1000000));

        });

        updateTimingInHearings(hearing, hearingTypeMap, defaultHearings);
        log.info("operation = enrichBulkReschedule, result=SUCCESS");
    }


}
