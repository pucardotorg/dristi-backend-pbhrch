// HAND-CURATED — lifted by Phase 35 (contract-lift)
package org.pucar.dristi.common.contract.summons;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class LocationBasedJurisdiction {

    @JsonProperty("included_jurisdiction")
    private PoliceStationDetails includedJurisdiction;

    @JsonProperty("nearest_police_station")
    private PoliceStationDetails nearestPoliceStation;
}
