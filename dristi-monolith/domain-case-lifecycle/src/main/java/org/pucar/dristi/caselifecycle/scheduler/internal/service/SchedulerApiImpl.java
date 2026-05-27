package org.pucar.dristi.caselifecycle.scheduler.internal.service;

import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.pucar.dristi.caselifecycle.scheduler.SchedulerApi;
import org.pucar.dristi.common.contract.scheduler.BulkRescheduleRequest;
import org.pucar.dristi.common.contract.scheduler.HearingSearchRequest;
import org.pucar.dristi.common.contract.scheduler.JudgeCalendarRule;
import org.pucar.dristi.common.contract.scheduler.JudgeCalendarUpdateRequest;
import org.pucar.dristi.common.contract.scheduler.ScheduleHearing;
import org.pucar.dristi.common.contract.scheduler.ScheduleHearingRequest;
import org.pucar.dristi.common.contract.scheduler.ScheduleHearingSearchCriteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class SchedulerApiImpl implements SchedulerApi {

    private final HearingService hearingService;
    private final ReScheduleHearingService reScheduleHearingService;
    private final CalendarService calendarService;

    @Autowired
    public SchedulerApiImpl(HearingService hearingService,
                            ReScheduleHearingService reScheduleHearingService,
                            CalendarService calendarService) {
        this.hearingService = hearingService;
        this.reScheduleHearingService = reScheduleHearingService;
        this.calendarService = calendarService;
    }

    @Override
    public List<ScheduleHearing> getScheduledHearings(RequestInfo requestInfo, ScheduleHearingSearchCriteria criteria) {
        HearingSearchRequest request = HearingSearchRequest.builder()
                .requestInfo(requestInfo)
                .criteria(criteria)
                .build();
        return hearingService.search(request, 1000, 0);
    }

    @Override
    public List<ScheduleHearing> bulkReschedule(RequestInfo requestInfo, BulkRescheduleRequest request) {
        return reScheduleHearingService.bulkReschedule(request);
    }

    @Override
    public void updateScheduleHearings(RequestInfo requestInfo, List<ScheduleHearing> scheduleHearings) {
        ScheduleHearingRequest request = ScheduleHearingRequest.builder()
                .requestInfo(requestInfo)
                .hearing(scheduleHearings)
                .build();
        hearingService.update(request);
    }

    @Override
    public List<ScheduleHearing> createScheduleHearing(RequestInfo requestInfo, List<ScheduleHearing> hearings) {
        ScheduleHearingRequest request = ScheduleHearingRequest.builder()
                .requestInfo(requestInfo)
                .hearing(hearings)
                .build();
        return hearingService.scheduleHearingInScheduler(request);
    }

    @Override
    public List<JudgeCalendarRule> updateJudgeCalendar(RequestInfo requestInfo, JudgeCalendarUpdateRequest request) {
        return calendarService.upsert(request);
    }
}
