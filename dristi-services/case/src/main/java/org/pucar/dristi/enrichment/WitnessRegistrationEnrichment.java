package org.pucar.dristi.enrichment;


import static org.pucar.dristi.config.ServiceConstants.ENRICHMENT_EXCEPTION;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.UUID;

import org.egov.common.contract.models.AuditDetails;
import org.egov.tracer.model.CustomException;
import org.pucar.dristi.config.Configuration;
import org.pucar.dristi.web.models.Witness;
import org.pucar.dristi.web.models.WitnessRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;

@Component
@Slf4j
public class WitnessRegistrationEnrichment {

    private final Configuration config;

    @Autowired
    public WitnessRegistrationEnrichment(Configuration config) {
        this.config = config;
    }

    public void enrichWitnessRegistration(WitnessRequest witnessRequest) {
        try {
            if (witnessRequest.getRequestInfo().getUserInfo() != null) {
                Witness witness = witnessRequest.getWitness();
                long now = OffsetDateTime.now(ZoneId.of(config.getZoneId())).toInstant().toEpochMilli();
                AuditDetails auditDetails = AuditDetails.builder().createdBy(witnessRequest.getRequestInfo().getUserInfo().getUuid()).createdTime(now).lastModifiedBy(witnessRequest.getRequestInfo().getUserInfo().getUuid()).lastModifiedTime(now).build();
                witness.setAuditDetails(auditDetails);

                witness.setId(UUID.randomUUID());

                witness.setIsActive(false);

                witness.setFilingNumber(UUID.randomUUID().toString());
                witness.setCnrNumber(witness.getFilingNumber());
            }
        } catch (Exception e) {
            log.error("Error enriching witness application :: {}", e.toString());
            throw new CustomException(ENRICHMENT_EXCEPTION, e.getMessage());
        }
    }

    public void enrichWitnessApplicationUponUpdate(WitnessRequest witnessRequest) {
        try {
            // Enrich lastModifiedTime and lastModifiedBy in witness of update
            Witness witness = witnessRequest.getWitness();
            long now = OffsetDateTime.now(ZoneId.of(config.getZoneId())).toInstant().toEpochMilli();
            witness.getAuditDetails().setLastModifiedTime(now);
            witness.getAuditDetails().setLastModifiedBy(witnessRequest.getRequestInfo().getUserInfo().getUuid());
        } catch (Exception e) {
            log.error("Error enriching witness application upon update :: {}", e.toString());
            throw new CustomException(ENRICHMENT_EXCEPTION, "Error in witness enrichment service during witness update process: " + e.getMessage());
        }
    }
}