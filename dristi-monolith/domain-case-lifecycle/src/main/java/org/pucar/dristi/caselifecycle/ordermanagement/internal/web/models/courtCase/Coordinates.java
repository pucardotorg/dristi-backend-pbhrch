package org.pucar.dristi.caselifecycle.ordermanagement.internal.web.models.courtCase;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Coordinates {
    @JsonProperty("latitude")
    private String latitude;
    @JsonProperty("longitude")
    private String longitude;
}
