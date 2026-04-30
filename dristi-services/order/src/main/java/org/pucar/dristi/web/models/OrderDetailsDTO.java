package org.pucar.dristi.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderDetailsDTO {

    @JsonProperty("id")
    @NotNull(message = "Id is required")
    private String id = null;

    @JsonProperty("tenantId")
    @NotNull
    private String tenantId = null;

    @JsonProperty("filingNumber")
    @NotNull(message = "Filing number is required")
    private String filingNumber = null;

    @JsonProperty("additionalDetails")
    private Object additionalDetails = null;

    @JsonProperty("compositeItems")
    private Object compositeItems = null;

    @JsonProperty("orderNumber")
    @NotNull(message = "Order number is required")
    private String orderNumber = null;

    @JsonProperty("uniqueId")
    @NotNull(message = "Unique ID is required")
    private String uniqueId = null;

    @JsonProperty("auditDetails")
    @Valid
    private AuditDetails auditDetails = null;
}
