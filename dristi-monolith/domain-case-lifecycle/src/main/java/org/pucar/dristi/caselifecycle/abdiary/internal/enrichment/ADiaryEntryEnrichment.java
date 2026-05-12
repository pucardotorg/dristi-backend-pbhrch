package org.pucar.dristi.caselifecycle.abdiary.internal.enrichment;

import org.pucar.dristi.caselifecycle.abdiary.internal.util.ADiaryUtil;
import org.pucar.dristi.common.contract.abdiary.CaseDiaryEntry;
import org.pucar.dristi.common.contract.abdiary.CaseDiaryEntryRequest;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.models.AuditDetails;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.User;
import org.egov.tracer.model.CustomException;
import org.springframework.stereotype.Component;

import static org.pucar.dristi.caselifecycle.abdiary.internal.config.ServiceConstants.ENRICHMENT_EXCEPTION;

import org.pucar.dristi.common.contract.abdiary.CaseDiarySearchRequest;
@Component
@Slf4j
public class ADiaryEntryEnrichment {

    private final ADiaryUtil aDiaryUtil;

    public ADiaryEntryEnrichment(ADiaryUtil aDiaryUtil) {
        this.aDiaryUtil = aDiaryUtil;
    }

    public void enrichCreateDiaryEntry(CaseDiaryEntryRequest caseDiaryEntryRequest) {

        log.info("operation = enrichCreateDiaryEntry ,  result = IN_PROGRESS , CaseDiarySearchRequest : {} ", caseDiaryEntryRequest);

        try {

            CaseDiaryEntry caseDiaryEntry = caseDiaryEntryRequest.getDiaryEntry();
            RequestInfo requestInfo = caseDiaryEntryRequest.getRequestInfo();
            User user = requestInfo.getUserInfo();

            caseDiaryEntry.setId(aDiaryUtil.generateUUID());

            AuditDetails auditDetails = AuditDetails.builder().createdBy(user.getUuid()).createdTime(aDiaryUtil.getCurrentTimeInMilliSec())
                    .lastModifiedBy(user.getUuid()).lastModifiedTime(aDiaryUtil.getCurrentTimeInMilliSec()).build();

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

            caseDiaryEntry.getAuditDetails().setLastModifiedTime(aDiaryUtil.getCurrentTimeInMilliSec());
            caseDiaryEntry.getAuditDetails().setLastModifiedBy(user.getUuid());

        } catch (Exception e) {
            log.error("Error occurred during enriching diary entry");
            throw new CustomException(ENRICHMENT_EXCEPTION, "Error during enriching diary entry");
        }

        log.info("operation = enrichUpdateEntry ,  result = SUCCESS , CaseDiarySearchRequest : {} ", caseDiaryEntryRequest);

    }

}
