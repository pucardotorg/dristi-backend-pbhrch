package org.pucar.dristi.web.models.cases;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.pucar.dristi.web.models.AuditDetails;
import org.springframework.validation.annotation.Validated;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;

@Schema(description = "Case overall status topic object")
@Validated
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Outcome {

    @JsonProperty("filingNumber")
    private String filingNumber = null;

    @JsonProperty("tenantId")
    private String tenantId = null;

    @JsonProperty("outcome")
    private String outcome = null;

    @JsonProperty("auditDetails")
    @Valid
    private AuditDetails auditDetails = null;

    public Outcome(String filingNumber, String tenantId, String outcome) {
        this.filingNumber = filingNumber;
        this.tenantId = tenantId;
        this.outcome = outcome;
    }

}
