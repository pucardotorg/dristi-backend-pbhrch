// HAND-CURATED — lifted by Phase 35 (contract-lift)
package org.pucar.dristi.common.contract.evidence;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import lombok.*;
import org.egov.common.contract.request.RequestInfo;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HearingSearchRequest {

    @JsonProperty("RequestInfo")
    @Valid
    private RequestInfo requestInfo;

    @JsonProperty("criteria")
    @Valid
    private HearingCriteria criteria;

    @JsonProperty("pagination")
    @Valid
    private Pagination pagination;

}

