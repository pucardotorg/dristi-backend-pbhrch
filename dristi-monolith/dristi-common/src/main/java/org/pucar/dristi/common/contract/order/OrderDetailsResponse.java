// HAND-CURATED — lifted by Phase 3.5 (contract-lift)
package org.pucar.dristi.common.contract.order;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.egov.common.contract.response.ResponseInfo;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderDetailsResponse {

    @JsonProperty("ResponseInfo")
    @Valid
    private ResponseInfo responseInfo = null;

    @JsonProperty("orderDetailsDTO")
    @Valid
    private OrderDetailsDTO orderDetailsDTO = null;

    @JsonProperty("message")
    private String message = null;
}
