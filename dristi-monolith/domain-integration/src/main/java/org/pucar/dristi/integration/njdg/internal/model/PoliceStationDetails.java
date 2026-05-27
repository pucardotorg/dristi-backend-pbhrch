package org.pucar.dristi.integration.njdg.internal.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PoliceStationDetails {

    private Integer policeStationCode;
    private String stName;
    private String natCode;
    private String policeCode;
}
