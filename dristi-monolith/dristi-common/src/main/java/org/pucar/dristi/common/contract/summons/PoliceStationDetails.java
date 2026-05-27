// HAND-CURATED — lifted by Phase 35 (contract-lift)
package org.pucar.dristi.common.contract.summons;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PoliceStationDetails {

    @JsonProperty("police_station")
    private String station;

    @JsonProperty("police_station_code")
    private String code;

    @JsonProperty("name")
    private String name;

    @JsonProperty("district")
    private String district;

    @JsonProperty("active")
    private Boolean active;

}
