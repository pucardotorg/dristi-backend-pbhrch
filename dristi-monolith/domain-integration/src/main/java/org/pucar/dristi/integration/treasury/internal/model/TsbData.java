package org.pucar.dristi.integration.treasury.internal.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.Data;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TsbData {

    @JsonProperty("TSB_ACCTYPE")
    private String tsbAccType;

    @JsonProperty("TSB_ACCNO")
    private String tsbAccNo;

    @JsonProperty("TSB_AMOUNT")
    private Double tsbAmount; // Double for monetary values

    @JsonProperty("TSB_PURPOSE")
    private String tsbPurpose;
}