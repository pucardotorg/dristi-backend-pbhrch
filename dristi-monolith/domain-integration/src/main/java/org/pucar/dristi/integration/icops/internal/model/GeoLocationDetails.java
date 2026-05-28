package org.pucar.dristi.integration.icops.internal.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.pucar.dristi.common.contract.icops.PoliceStationDetails;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeoLocationDetails {

    @JsonProperty("policeStation")
    private PoliceStationDetails policeStationDetails;
}
