package org.pucar.dristi.integration.epost.internal.model;

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
