// HAND-CURATED — lifted by Phase 35 (contract-lift)
package org.pucar.dristi.common.contract.summons;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.egov.common.contract.request.RequestInfo;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DemandCreateRequest {

    @JsonProperty("RequestInfo")
    private RequestInfo requestInfo = null;

    @JsonProperty("consumerCode")
    private String consumerCode = null;

    @JsonProperty("calculation")
    private List<Calculation> calculation = null;

    @JsonProperty("filingNumber")
    private String filingNumber = null;

    @JsonProperty("entityType")
    private String entityType = null;

    @JsonProperty("deliveryChannel")
    private String deliveryChannel = null;

    @JsonProperty("tenantId")
    private String tenantId = null;


}
