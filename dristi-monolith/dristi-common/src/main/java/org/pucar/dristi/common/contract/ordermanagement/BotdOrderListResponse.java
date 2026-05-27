// HAND-CURATED — lifted by Phase 35 (contract-lift)
package org.pucar.dristi.common.contract.ordermanagement;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BotdOrderListResponse {

    @JsonProperty("botdOrderList")
    @Valid
    private List<BotdOrderSummary> botdOrderList = null;

    @JsonProperty("totalCount")
    private Integer totalCount = null;

    @JsonProperty("pagination")
    @Valid
    private Pagination pagination = null;

}
