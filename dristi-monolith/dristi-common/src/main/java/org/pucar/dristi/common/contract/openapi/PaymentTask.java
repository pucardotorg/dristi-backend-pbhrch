// HAND-CURATED — lifted by Phase 35 (contract-lift)
package org.pucar.dristi.common.contract.openapi;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PaymentTask {
    @JsonProperty("dueDate")
    private Long dueDate;

    @JsonProperty("daysRemaining")
    private Integer daysRemaining;

    @JsonProperty("task")
    private String task;
}
