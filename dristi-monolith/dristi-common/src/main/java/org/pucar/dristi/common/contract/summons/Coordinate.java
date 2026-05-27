// HAND-CURATED — lifted by Phase 35 (contract-lift)
package org.pucar.dristi.common.contract.summons;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Coordinate {

    @JsonProperty("latitude")
    private  String latitude;

    @JsonProperty("longitude")
    private String longitude;
}
