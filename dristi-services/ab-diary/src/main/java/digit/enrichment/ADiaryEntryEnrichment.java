package digit.enrichment;

import digit.util.ADiaryUtil;
import digit.util.DateTimeUtil;
import digit.web.models.AuditDetails;
import digit.web.models.CaseDiaryEntry;
import digit.web.models.CaseDiaryEntryRequest;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.User;
import org.egov.tracer.model.CustomException;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

import static digit.config.ServiceConstants.ENRICHMENT_EXCEPTION;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;

@Component
@Slf4j
public class ADiaryEntryEnrichment {

    private final ADiaryUtil aDiaryUtil;

    private final DateTimeUtil dateTimeUtil;

    public ADiaryEntryEnrichment(ADiaryUtil aDiaryUtil, DateTimeUtil dateTimeUtil) {
        this.aDiaryUtil = aDiaryUtil;
        this.dateTimeUtil = dateTimeUtil;
    }

    public void enrichCreateDiaryEntry(CaseDiaryEntryRequest caseDiaryEntryRequest) {

        log.info("operation = enrichCreateDiaryEntry ,  result = IN_PROGRESS , CaseDiarySearchRequest : {} ", caseDiaryEntryRequest);

        try {

            CaseDiaryEntry caseDiaryEntry = caseDiaryEntryRequest.getDiaryEntry();
            RequestInfo requestInfo = caseDiaryEntryRequest.getRequestInfo();
            User user = requestInfo.getUserInfo();

            caseDiaryEntry.setId(aDiaryUtil.generateUUID());

            OffsetDateTime now = dateTimeUtil.getCurrentOffsetDateTime();
            AuditDetails auditDetails = AuditDetails.builder().createdBy(user.getUuid()).createdTime(now)
                    .lastModifiedBy(user.getUuid()).lastModifiedTime(now).build();

            caseDiaryEntry.setAuditDetails(auditDetails);

        } catch (CustomException e) {
            log.error("Error occurred while enriching diary entry");
            throw new CustomException(ENRICHMENT_EXCEPTION, "Error during enriching diary entry");
        }

        log.info("operation = enrichCreateDiaryEntry ,  result = SUCCESS , CaseDiarySearchRequest : {} ", caseDiaryEntryRequest);

    }

    public void enrichUpdateEntry(CaseDiaryEntryRequest caseDiaryEntryRequest) {

        log.info("operation = enrichUpdateEntry ,  result = IN_PROGRESS , CaseDiarySearchRequest : {} ", caseDiaryEntryRequest);

        try {

            CaseDiaryEntry caseDiaryEntry = caseDiaryEntryRequest.getDiaryEntry();
            RequestInfo requestInfo = caseDiaryEntryRequest.getRequestInfo();
            User user = requestInfo.getUserInfo();

            caseDiaryEntry.getAuditDetails().setLastModifiedTime(dateTimeUtil.getCurrentOffsetDateTime());
            caseDiaryEntry.getAuditDetails().setLastModifiedBy(user.getUuid());

        } catch (Exception e) {
            log.error("Error occurred during enriching diary entry");
            throw new CustomException(ENRICHMENT_EXCEPTION, "Error during enriching diary entry");
        }

        log.info("operation = enrichUpdateEntry ,  result = SUCCESS , CaseDiarySearchRequest : {} ", caseDiaryEntryRequest);

    }

}
