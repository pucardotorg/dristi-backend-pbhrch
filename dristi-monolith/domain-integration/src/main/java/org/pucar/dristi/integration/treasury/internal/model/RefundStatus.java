package org.pucar.dristi.integration.treasury.internal.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class RefundStatus {

    @JsonProperty("GRN")
    private String grn;

    @JsonProperty("RefundRequestNumber")
    private String refundRequestNumber;
}
