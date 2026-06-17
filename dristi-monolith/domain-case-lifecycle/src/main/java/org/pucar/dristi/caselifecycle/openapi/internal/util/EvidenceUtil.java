package org.pucar.dristi.caselifecycle.openapi.internal.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.pucar.dristi.caselifecycle.evidence.EvidenceApi;
import org.pucar.dristi.caselifecycle.openapi.internal.config.Configuration;
import org.pucar.dristi.caselifecycle.openapi.internal.web.models.witnessdeposition.Artifact;
import org.pucar.dristi.caselifecycle.openapi.internal.web.models.witnessdeposition.EvidenceRequest;
import org.pucar.dristi.caselifecycle.openapi.internal.web.models.witnessdeposition.EvidenceResponse;
import org.pucar.dristi.caselifecycle.openapi.internal.web.models.witnessdeposition.EvidenceSearchCriteria;
import org.pucar.dristi.caselifecycle.openapi.internal.web.models.witnessdeposition.EvidenceSearchResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component("openapiEvidenceUtil")
@Slf4j
public class EvidenceUtil {

    private final Configuration configuration;
    private final RestTemplate restTemplate;
    private final ObjectMapper mapper;
    private final EvidenceApi evidenceApi;

    @Autowired
    public EvidenceUtil(Configuration configuration, RestTemplate restTemplate, ObjectMapper mapper, EvidenceApi evidenceApi) {
        this.configuration = configuration;
        this.restTemplate = restTemplate;
        this.mapper = mapper;
        this.evidenceApi = evidenceApi;
    }

    public EvidenceSearchResponse searchEvidence(EvidenceSearchCriteria criteria, RequestInfo requestInfo) {
        try {
            org.pucar.dristi.common.contract.evidence.EvidenceSearchCriteria bridgedCriteria =
                    mapper.convertValue(criteria,
                            org.pucar.dristi.common.contract.evidence.EvidenceSearchCriteria.class);
            org.pucar.dristi.common.contract.evidence.EvidenceSearchResponse response =
                    evidenceApi.searchEvidence(requestInfo, bridgedCriteria, null);
            return mapper.convertValue(response, EvidenceSearchResponse.class);
        } catch (Exception e) {
            log.error("Error while searching for evidence", e);
            throw new CustomException("EVIDENCE_SERVICE_ERROR", e.getMessage());
        }
    }

    // Rule 35: writes stay REST until cross-subdomain write semantics are designed.
    public EvidenceResponse updateEvidence(Artifact artifact, RequestInfo requestInfo) {
        StringBuilder uri = new StringBuilder();
        uri.append(configuration.getEvidenceServiceHost()).append(configuration.getEvidenceServiceUpdateEndpoint());

        EvidenceRequest artifactRequest = EvidenceRequest.builder()
                .requestInfo(requestInfo)
                .artifact(artifact)
                .build();

        try {
            Object response = restTemplate.postForObject(uri.toString(), artifactRequest, Map.class);
            return mapper.convertValue(response, EvidenceResponse.class);
        } catch (Exception e) {
            log.error("Error while updating evidence", e);
            throw new CustomException("EVIDENCE_SERVICE_ERROR", e.getMessage());
        }
    }
}
