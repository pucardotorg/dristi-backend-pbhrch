package digit.enrichment;


import digit.util.DateUtil;
import digit.web.models.AuditDetails;
import digit.web.models.OptOutRequest;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.UUID;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;

@Component
@Slf4j
public class RescheduleRequestOptOutEnrichment {

    private final DateUtil dateUtil;

    @Autowired
    public RescheduleRequestOptOutEnrichment(DateUtil dateUtil) {
        this.dateUtil = dateUtil;
    }

    public void enrichCreateRequest(OptOutRequest request) {
        log.info("operation = enrichCreateRequest, result = IN_PROGRESS, OptOut = {}", request.getOptOut());
        AuditDetails auditDetails = getAuditDetailsScheduleHearing(request.getRequestInfo());
        request.getOptOut().setId(UUID.randomUUID().toString());
        request.getOptOut().setAuditDetails(auditDetails);
        request.getOptOut().setRowVersion(1);
        log.info("operation = enrichCreateRequest, result = SUCCESS, OptOut = {}", request.getOptOut());

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
