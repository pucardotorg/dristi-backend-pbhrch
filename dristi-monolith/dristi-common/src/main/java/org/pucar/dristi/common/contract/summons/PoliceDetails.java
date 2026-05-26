// HAND-CURATED — lifted by Phase 35 (contract-lift)
package org.pucar.dristi.common.contract.summons;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PoliceDetails {

    @JsonProperty("uniqueId")
    private String uniqueId;

    @JsonProperty("name")
    private String name;

    @JsonProperty("district")
    private String district;

    @JsonProperty("active")
    private Boolean active;
}
