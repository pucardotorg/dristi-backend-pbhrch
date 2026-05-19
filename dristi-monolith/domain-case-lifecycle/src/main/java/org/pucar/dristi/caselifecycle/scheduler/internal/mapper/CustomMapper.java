package org.pucar.dristi.caselifecycle.scheduler.internal.mapper;

import org.pucar.dristi.common.contract.scheduler.ScheduleHearing;
import org.pucar.dristi.caselifecycle.scheduler.internal.web.models.hearing.Hearing;
import org.springframework.stereotype.Component;

@Component
public class CustomMapper {

    public ScheduleHearing hearingToScheduleHearingConversion(Hearing hearing) {
        ScheduleHearing scheduleHearing = new ScheduleHearing();
        scheduleHearing.setHearingBookingId(hearing.getHearingId());
        scheduleHearing.setHearingType(hearing.getHearingType());
        scheduleHearing.setStatus(hearing.getStatus());
        if (hearing.getPresidedBy() != null) {
            scheduleHearing.setCourtId(hearing.getPresidedBy().getCourtID());
        }
        return scheduleHearing;
    }
}
