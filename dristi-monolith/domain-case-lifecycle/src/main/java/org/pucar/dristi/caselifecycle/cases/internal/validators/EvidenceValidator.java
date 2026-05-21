package org.pucar.dristi.caselifecycle.cases.internal.validators;

import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.pucar.dristi.caselifecycle.cases.internal.web.models.*;
import org.pucar.dristi.caselifecycle.evidence.EvidenceApi;
import org.pucar.dristi.common.contract.evidence.EvidenceSearchCriteria;
import org.pucar.dristi.common.contract.evidence.EvidenceSearchResponse;
import org.pucar.dristi.common.contract.evidence.Pagination;
import org.pucar.dristi.common.models.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("casesEvidenceValidator")
@Slf4j
public class EvidenceValidator {

    private final EvidenceApi evidenceApi;

    @Autowired
    public EvidenceValidator(EvidenceApi evidenceApi) {
        this.evidenceApi = evidenceApi;
    }


    public boolean validateEvidenceCreation(CourtCase courtCase, RequestInfo requestInfo, ReplacementDetails replacementDetails) {

        EvidenceSearchCriteria criteria = EvidenceSearchCriteria.builder()
                .caseId(courtCase.getId().toString())
                .filingNumber(courtCase.getFilingNumber())
                .tenantId(courtCase.getTenantId())
                .fileStoreId(replacementDetails.getDocument().getFileStore())
                .build();

        EvidenceSearchResponse response = evidenceApi.searchEvidence(requestInfo, criteria, defaultPagination());
        return !response.getArtifacts().isEmpty();
    }

    public boolean validateEvidenceCreate(CourtCase courtCase, RequestInfo requestInfo, List<Document> documentList) {

        if (documentList != null && !documentList.isEmpty()) {
            EvidenceSearchCriteria criteria = EvidenceSearchCriteria.builder()
                    .caseId(courtCase.getId().toString())
                    .filingNumber(courtCase.getFilingNumber())
                    .tenantId(courtCase.getTenantId())
                    .fileStoreId(documentList.get(0).getFileStore())
                    .build();

            EvidenceSearchResponse response = evidenceApi.searchEvidence(requestInfo, criteria, defaultPagination());
            return !response.getArtifacts().isEmpty();
        }
        return true;
    }

    public boolean validateReasonDocumentCreation(CourtCase courtCase, RequestInfo requestInfo, ReasonDocument reasonDocument) {

        EvidenceSearchCriteria criteria = EvidenceSearchCriteria.builder()
                .caseId(courtCase.getId().toString())
                .filingNumber(courtCase.getFilingNumber())
                .tenantId(courtCase.getTenantId())
                .fileStoreId(reasonDocument.getFileStore())
                .build();

        EvidenceSearchResponse response = evidenceApi.searchEvidence(requestInfo, criteria, defaultPagination());
        return !response.getArtifacts().isEmpty();
    }

    private static Pagination defaultPagination() {
        return Pagination.builder().limit(100.0).offSet(0.0).build();
    }
}
