package org.pucar.dristi.caselifecycle.analytics.internal.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReferenceEntityTypeNameMapping {

    @JsonProperty("referenceEntityType")
    private String referenceEntityType;

    @JsonProperty("pendingTaskName")
    private String pendingTaskName;

    @JsonProperty("actionCategory")
    private String actionCategory;
}

