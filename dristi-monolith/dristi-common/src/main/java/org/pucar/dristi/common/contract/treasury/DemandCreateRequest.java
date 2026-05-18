package org.pucar.dristi.common.contract.treasury;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.egov.common.contract.request.RequestInfo;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DemandCreateRequest {

    @JsonProperty("RequestInfo")
    private RequestInfo requestInfo;

    @JsonProperty("consumerCode")
    private String consumerCode;

    @JsonProperty("calculation")
    private List<Calculation> calculation;

    @JsonProperty("filingNumber")
    private String filingNumber;

    @JsonProperty("deliveryChannel")
    private String deliveryChannel;

    @JsonProperty("entityType")
    private String entityType;

    @JsonProperty("tenantId")
    private String tenantId;

    @JsonProperty("finalCalcPostResubmission")
    private Calculation finalCalcPostResubmission;

    @JsonProperty("lastSubmissionConsumerCode")
    private String lastSubmissionConsumerCode;
}
