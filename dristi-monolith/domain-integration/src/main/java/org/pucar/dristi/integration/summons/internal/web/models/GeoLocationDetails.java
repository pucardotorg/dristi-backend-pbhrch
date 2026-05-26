package org.pucar.dristi.integration.summons.internal.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeoLocationDetails {

    @JsonProperty("policeStation")
    private PoliceStationDetails policeStationDetails;
}
