package digit.enrichment;


import digit.util.DateUtil;
import digit.web.models.AuditDetails;
import digit.web.models.ReScheduleHearing;
import digit.web.models.ReScheduleHearingRequest;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.List;

import static digit.config.ServiceConstants.ACTIVE;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;

@Component
@Slf4j
public class ReScheduleRequestEnrichment {

    private final DateUtil dateUtil;

    @Autowired
    public ReScheduleRequestEnrichment(DateUtil dateUtil) {
        this.dateUtil = dateUtil;
    }

    public void enrichRescheduleRequest(ReScheduleHearingRequest reScheduleHearingsRequest) {
        log.info("operation = enrichRescheduleRequest , Result = IN_PROGRESS");
        List<ReScheduleHearing> reScheduleHearing = reScheduleHearingsRequest.getReScheduleHearing();
        RequestInfo requestInfo = reScheduleHearingsRequest.getRequestInfo();

        AuditDetails auditDetails = getAuditDetailsReScheduleHearing(requestInfo);

        for (ReScheduleHearing element : reScheduleHearing) {
            element.setRowVersion(1);
            element.setAuditDetails(auditDetails);
            element.setStatus(ACTIVE);
        }
        log.info("operation = enrichRescheduleRequest, Result=SUCCESS");
    }

    private AuditDetails getAuditDetailsReScheduleHearing(RequestInfo requestInfo) {
        OffsetDateTime now = dateUtil.getCurrentOffsetDateTime();
        return AuditDetails.builder()
                .createdBy(requestInfo.getUserInfo().getUuid())
                .createdTime(now)
                .lastModifiedBy(requestInfo.getUserInfo().getUuid())
                .lastModifiedTime(now)
                .build();
    }
}
