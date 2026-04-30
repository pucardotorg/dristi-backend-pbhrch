package org.pucar.dristi.web.models;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.UUID;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressResponse {

    @JsonProperty("id")
    private UUID id;

    @JsonProperty("individualId")
    private UUID individualId;

    @JsonProperty("addressDetails")
    private Address addressDetails;

    @JsonProperty("auditDetails")
    private AuditDetails auditDetails;
}
