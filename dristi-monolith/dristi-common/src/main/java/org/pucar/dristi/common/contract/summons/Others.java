// HAND-CURATED — lifted by Phase 35 (contract-lift)
package org.pucar.dristi.common.contract.summons;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Others {

    @JsonProperty("uniqueId")
    private String uniqueId;

    @JsonProperty("name")
    private String name;
}
