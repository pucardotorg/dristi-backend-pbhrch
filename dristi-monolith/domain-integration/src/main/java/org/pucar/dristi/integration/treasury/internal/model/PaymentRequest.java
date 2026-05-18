package org.pucar.dristi.integration.treasury.internal.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.Data;
import org.egov.common.contract.request.RequestInfo;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentRequest {

    @JsonProperty("RequestInfo")
    private RequestInfo requestInfo;

    @JsonProperty("Payment")
    private Payment payment;
}
