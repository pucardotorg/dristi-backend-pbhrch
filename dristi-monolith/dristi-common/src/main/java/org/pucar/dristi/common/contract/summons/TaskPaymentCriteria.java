// HAND-CURATED — lifted by Phase 35 (contract-lift)
package org.pucar.dristi.common.contract.summons;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class TaskPaymentCriteria {

    @JsonProperty("channelId")
    private String channelId;

    @JsonProperty("receiverPincode")
    private String receiverPincode;

    @JsonProperty("tenantId")
    private String tenantId = null;

    @JsonProperty("taskType")
    private String taskType;

    @JsonProperty("id")
    private String id;


}
