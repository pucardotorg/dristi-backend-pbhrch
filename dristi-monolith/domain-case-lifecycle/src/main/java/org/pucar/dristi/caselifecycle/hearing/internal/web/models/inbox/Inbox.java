package org.pucar.dristi.caselifecycle.hearing.internal.web.models.inbox;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;


import org.pucar.dristi.common.contract.hearing.ProcessInstance;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Inbox {

    @JsonProperty("ProcessInstance")
    private ProcessInstance ProcessInstance;

    @JsonProperty("businessObject")
    private Map<String,Object> businessObject;

    @JsonProperty("serviceObject")
    private Map<String,Object>	serviceObject;
}
