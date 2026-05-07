// HAND-CURATED — lifted by Phase 3.5 (contract-lift)
package org.pucar.dristi.common.contract.order;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.egov.common.contract.request.RequestInfo;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderDetailsRequest {

    @JsonProperty("RequestInfo")
    @Valid
    @NotNull
    private RequestInfo requestInfo = null;

    @JsonProperty("orderDetailsDTO")
    @Valid
    @NotNull
    private OrderDetailsDTO orderDetailsDTO = null;
}
