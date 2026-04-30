package org.pucar.dristi.caselifecycle.cases.internal.web.models;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.UUID;

import org.pucar.dristi.common.models.AuditDetails;
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
