package org.pucar.dristi.caselifecycle.task.internal.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TaskToSign {
    @JsonProperty("taskNumber")
    private String taskNumber = null;

    @JsonProperty("request")
    private String request;
}
