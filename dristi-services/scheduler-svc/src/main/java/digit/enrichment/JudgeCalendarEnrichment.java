package digit.enrichment;

import digit.util.DateUtil;
import digit.web.models.AuditDetails;
import digit.web.models.JudgeCalendarRule;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;

@Component
@Slf4j
public class JudgeCalendarEnrichment {

    private final DateUtil dateUtil;

    @Autowired
    public JudgeCalendarEnrichment(DateUtil dateUtil) {
        this.dateUtil = dateUtil;
    }

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
        OffsetDateTime now = dateUtil.getCurrentOffsetDateTime();
        return AuditDetails.builder()
                .createdBy(requestInfo.getUserInfo().getUuid())
                .createdTime(now)
                .lastModifiedBy(requestInfo.getUserInfo().getUuid())
                .lastModifiedTime(now)
                .build();
    }


}
