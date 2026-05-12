package org.pucar.dristi.caselifecycle.ctc.internal.web.models.courtcase;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Coordinates {
    @JsonProperty("latitude")
    private String latitude;
    @JsonProperty("longitude")
    private String longitude;
}
