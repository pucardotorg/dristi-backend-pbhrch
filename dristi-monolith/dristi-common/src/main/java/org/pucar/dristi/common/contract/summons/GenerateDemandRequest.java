// HAND-CURATED — lifted by Phase 35 (contract-lift)
package org.pucar.dristi.common.contract.summons;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.egov.common.contract.request.RequestInfo;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerateDemandRequest {
    @NotNull
    @JsonProperty("RequestInfo")
    private RequestInfo requestInfo;

    @Valid
    @NotNull
    @JsonProperty("Calculations")
    private List<Calculation> calculations = new ArrayList<>();
}

