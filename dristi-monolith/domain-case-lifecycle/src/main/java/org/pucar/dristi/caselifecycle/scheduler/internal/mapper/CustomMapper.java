package org.pucar.dristi.caselifecycle.scheduler.internal.mapper;

import org.pucar.dristi.common.contract.scheduler.ScheduleHearing;
import org.pucar.dristi.caselifecycle.scheduler.internal.web.models.hearing.Hearing;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CustomMapper {

    @Mapping(source = "hearingId", target = "hearingBookingId")
    @Mapping(source = "hearingType", target = "hearingType")
    @Mapping(source = "presidedBy.courtID", target = "courtId")
    @Mapping(source = "status", target = "status")
    ScheduleHearing hearingToScheduleHearingConversion(Hearing hearing);
}
