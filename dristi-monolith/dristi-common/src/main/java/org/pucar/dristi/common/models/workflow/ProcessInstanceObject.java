// HAND-CURATED — DRISTI's standard extension of egov's ProcessInstance.
// Mirror of WorkflowObject — adds `additionalDetails` so the workflow
// service receives the field DRISTI services consistently include in
// their transition requests.
package org.pucar.dristi.common.models.workflow;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.egov.common.contract.workflow.ProcessInstance;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProcessInstanceObject extends ProcessInstance {
    @JsonProperty("additionalDetails")
    private Object additionalDetails = null;
}
