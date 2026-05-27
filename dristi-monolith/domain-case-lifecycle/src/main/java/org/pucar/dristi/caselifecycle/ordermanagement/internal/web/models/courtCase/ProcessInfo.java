package org.pucar.dristi.caselifecycle.ordermanagement.internal.web.models.courtCase;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.pucar.dristi.common.models.Document;

@Validated
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcessInfo {

    @JsonProperty("tenantId")
    private String tenantId;

    @JsonProperty("caseId")
    private String caseId;

    @JsonProperty("action")
    private String action;

    @JsonProperty("pendingTaskRefId")
    private String pendingTaskRefId;

}