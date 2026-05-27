package org.pucar.dristi.caselifecycle.hearingmanagement.internal.config;

import lombok.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component("hearingmanagementConfiguration")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class Configuration {

    @Value("${egov.tenantId}")
    private String tenantId;

    @Value("${egov.court.holidays.mdms.path}")
    private String courtHolidayMdmsPath;

    @Value("${egov.schedule.hearing.module}")
    private String scheduleHearingModuleName;

    @Value("${egov.court.holiday.master}")
    private String courtHolidayMasterName;

    @Value("${app.zone.id}")
    private String zoneId;
}
