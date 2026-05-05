// HAND-CURATED — lifted by Phase 3.5 (contract-lift)
package org.pucar.dristi.common.contract.order;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.egov.common.contract.models.Workflow;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowObject extends Workflow {
    @JsonProperty("additionalDetails")
    private Object additionalDetails = null;
}
