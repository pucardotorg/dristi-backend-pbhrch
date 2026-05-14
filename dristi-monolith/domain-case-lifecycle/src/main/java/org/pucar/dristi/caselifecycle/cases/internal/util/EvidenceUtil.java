package org.pucar.dristi.caselifecycle.cases.internal.util;

import lombok.extern.slf4j.Slf4j;
import org.egov.tracer.model.CustomException;
import org.pucar.dristi.caselifecycle.cases.internal.config.Configuration;
import org.pucar.dristi.caselifecycle.cases.internal.web.models.EvidenceRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import static org.pucar.dristi.caselifecycle.cases.internal.config.ServiceConstants.EVIDENCE_CREATE_ERROR;

/**
 * REST shim for the one remaining cases→evidence write path
 * ({@code createEvidence}). The read path was lifted onto
 * {@link org.pucar.dristi.caselifecycle.evidence.EvidenceApi#searchEvidence}
 * in the evidence migration; {@code createEvidence} stays over REST
 * for now because cross-subdomain writes are a Rule 35 design call
 * that hasn't been made yet.
 */
@Slf4j
@Component
public class EvidenceUtil {

    private final RestTemplate restTemplate;
    private final Configuration config;

    @Autowired
    public EvidenceUtil(RestTemplate restTemplate, Configuration config) {
        this.restTemplate = restTemplate;
        this.config = config;
    }

    public void createEvidence(EvidenceRequest evidenceRequest) {

        StringBuilder uri = new StringBuilder();
        uri.append(config.getEvidenceServiceHost()).append(config.getEvidenceServiceCreatePath());
        try {
            restTemplate.postForEntity(uri.toString(), evidenceRequest, String.class);
        } catch (Exception e) {
            log.error("Error getting response from Evidence Service", e);
            throw new CustomException(EVIDENCE_CREATE_ERROR, "Error getting response from Evidence Service");
        }
    }
}
