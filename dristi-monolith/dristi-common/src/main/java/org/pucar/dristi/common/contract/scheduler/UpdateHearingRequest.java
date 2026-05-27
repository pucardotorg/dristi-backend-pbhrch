// HAND-CURATED — lifted by Phase 35 (contract-lift)
package org.pucar.dristi.common.contract.scheduler;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.egov.common.contract.request.RequestInfo;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateHearingRequest {

    @JsonProperty("RequestInfo")
    @Valid
    private RequestInfo requestInfo = null;

    @JsonProperty("Hearing")
    @Valid

    private ScheduleHearing hearing = null;
}
