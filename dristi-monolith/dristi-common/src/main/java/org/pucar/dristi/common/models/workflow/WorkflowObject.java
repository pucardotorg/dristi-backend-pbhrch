// HAND-CURATED — DRISTI's standard extension of egov's Workflow contract.
// Promoted to dristi-common after the order migration found every service
// duplicates this same `additionalDetails`-only subclass. See Rule 29.
package org.pucar.dristi.common.models.workflow;

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
