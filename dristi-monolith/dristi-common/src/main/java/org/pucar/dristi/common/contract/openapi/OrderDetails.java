// HAND-CURATED — lifted by Phase 35 (contract-lift)
package org.pucar.dristi.common.contract.openapi;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class OrderDetails {

    @JsonProperty("date")
    private Long date;

    @JsonProperty("businessOfTheDay")
    private String businessOfTheDay;

    @JsonProperty("orderId")
    private String orderId;

}
