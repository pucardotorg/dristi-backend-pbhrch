package org.pucar.dristi.caselifecycle.scheduler.internal.enrichment;

import org.pucar.dristi.common.models.AuditDetails;
import org.pucar.dristi.common.contract.scheduler.JudgeCalendarRule;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@Slf4j
public class JudgeCalendarEnrichment {


    public void enrichUpdateJudgeCalendar(RequestInfo requestInfo, List<JudgeCalendarRule> judgeCalendarRule) {
        log.info("operation = enrichUpdateJudgeCalendar , Result = IN_PROGRESS");
        AuditDetails auditDetails = getAuditDetailsScheduleHearing(requestInfo);

        judgeCalendarRule.forEach((calendar) -> {

            calendar.setId(UUID.randomUUID().toString());
            calendar.setAuditDetails(auditDetails);
            calendar.setRowVersion(1);

        });
        log.info("operation = enrichUpdateJudgeCalendar, Result=SUCCESS");
    }

    private AuditDetails getAuditDetailsScheduleHearing(RequestInfo requestInfo) {

        return AuditDetails.builder().createdBy(requestInfo.getUserInfo().getUuid()).createdTime(System.currentTimeMillis()).lastModifiedBy(requestInfo.getUserInfo().getUuid()).lastModifiedTime(System.currentTimeMillis()).build();

    }


}
