package org.pucar.dristi.caselifecycle.casemanagement.internal.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.pucar.dristi.caselifecycle.evidence.EvidenceApi;
import org.pucar.dristi.common.contract.casemanagement.Artifact;
import org.pucar.dristi.common.contract.evidence.EvidenceSearchCriteria;
import org.pucar.dristi.common.contract.evidence.EvidenceSearchResponse;
import org.pucar.dristi.common.contract.evidence.Pagination;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component("casemanagementEvidenceUtil")
@Slf4j
public class EvidenceUtil {

    private final EvidenceApi evidenceApi;
    private final ObjectMapper mapper;

    public EvidenceUtil(EvidenceApi evidenceApi, ObjectMapper mapper) {
        this.evidenceApi = evidenceApi;
        this.mapper = mapper;
    }

    public List<Artifact> searchEvidence(String filingNumber, String courtId, String tenantId, RequestInfo requestInfo) {
        EvidenceSearchCriteria criteria = EvidenceSearchCriteria.builder()
                .filingNumber(filingNumber)
                .courtId(courtId)
                .isVoid(false)
                .tenantId(tenantId)
                .isHideBailCaseBundle(true)
                .build();
        Pagination pagination = Pagination.builder().sortBy("createdTime").order(org.pucar.dristi.common.contract.evidence.Order.ASC).limit(100.0).build();
        try {
            EvidenceSearchResponse response = evidenceApi.searchEvidence(requestInfo, criteria, pagination);
            log.info("Evidence response :: {}", response);
            return convertArtifacts(response);
        } catch (Exception e) {
            log.error("Error while searching for evidence", e);
            throw new CustomException("EVIDENCE_SERVICE_ERROR", e.getMessage());
        }
    }

    /**
     * Fetches evidence specifically for the ADDITIONAL_FILINGS section.
     * Makes two calls: filingType=DIRECT and filingType=APPLICATION, both with evidenceStatus=false,
     * matching the UI's data fetching for this section.
     */
    public List<Artifact> searchAdditionalFilingEvidence(String filingNumber, String courtId, String tenantId, RequestInfo requestInfo) {
        List<Artifact> direct = searchEvidenceByFilingType(filingNumber, courtId, tenantId, "DIRECT", requestInfo);
        List<Artifact> application = searchEvidenceByFilingType(filingNumber, courtId, tenantId, "APPLICATION", requestInfo);

        List<Artifact> combined = new ArrayList<>(direct);
        combined.addAll(application);
        return combined;
    }

    private List<Artifact> searchEvidenceByFilingType(String filingNumber, String courtId, String tenantId, String filingType, RequestInfo requestInfo) {
        EvidenceSearchCriteria criteria = EvidenceSearchCriteria.builder()
                .filingNumber(filingNumber)
                .courtId(courtId)
                .isVoid(false)
                .tenantId(tenantId)
                .filingType(filingType)
                .evidenceStatus(false)
                .isHideBailCaseBundle(true)
                .build();
        Pagination pagination = Pagination.builder().sortBy("createdTime").order(org.pucar.dristi.common.contract.evidence.Order.ASC).limit(100.0).build();
        try {
            EvidenceSearchResponse response = evidenceApi.searchEvidence(requestInfo, criteria, pagination);
            return convertArtifacts(response);
        } catch (Exception e) {
            log.error("Error while searching for {} evidence", filingType, e);
            return Collections.emptyList();
        }
    }

    private List<Artifact> convertArtifacts(EvidenceSearchResponse response) {
        if (response == null || response.getArtifacts() == null || response.getArtifacts().isEmpty()) {
            return Collections.emptyList();
        }
        return mapper.convertValue(
                response.getArtifacts(),
                mapper.getTypeFactory().constructCollectionType(List.class, Artifact.class));
    }
}
