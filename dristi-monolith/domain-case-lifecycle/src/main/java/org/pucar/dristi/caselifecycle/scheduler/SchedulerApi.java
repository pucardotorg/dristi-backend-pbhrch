package org.pucar.dristi.caselifecycle.scheduler;

import org.egov.common.contract.request.RequestInfo;
import org.pucar.dristi.common.contract.scheduler.BulkRescheduleRequest;
import org.pucar.dristi.common.contract.scheduler.JudgeCalendarRule;
import org.pucar.dristi.common.contract.scheduler.JudgeCalendarUpdateRequest;
import org.pucar.dristi.common.contract.scheduler.ScheduleHearing;
import org.pucar.dristi.common.contract.scheduler.ScheduleHearingSearchCriteria;

import java.util.List;

/**
 * Public cross-subdomain API of the scheduler subdomain.
 * Other modules (hearing) consume scheduler through this interface — never by importing from internal/.
 */
public interface SchedulerApi {

    List<ScheduleHearing> getScheduledHearings(RequestInfo requestInfo, ScheduleHearingSearchCriteria criteria);

    List<ScheduleHearing> bulkReschedule(RequestInfo requestInfo, BulkRescheduleRequest request);

    void updateScheduleHearings(RequestInfo requestInfo, List<ScheduleHearing> scheduleHearings);

    List<ScheduleHearing> createScheduleHearing(RequestInfo requestInfo, List<ScheduleHearing> hearings);

    List<JudgeCalendarRule> updateJudgeCalendar(RequestInfo requestInfo, JudgeCalendarUpdateRequest request);
}
