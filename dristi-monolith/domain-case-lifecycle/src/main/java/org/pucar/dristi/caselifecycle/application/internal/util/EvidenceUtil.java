package org.pucar.dristi.caselifecycle.application.internal.util;

import lombok.extern.slf4j.Slf4j;
import org.egov.tracer.model.CustomException;
import org.pucar.dristi.caselifecycle.application.internal.config.Configuration;
import org.pucar.dristi.common.contract.application.EvidenceRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component("applicationEvidenceUtil")
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
            throw new CustomException("EVIDENCE_CREATE_ERROR", "Error getting response from Evidence Service");
        }
    }

}
