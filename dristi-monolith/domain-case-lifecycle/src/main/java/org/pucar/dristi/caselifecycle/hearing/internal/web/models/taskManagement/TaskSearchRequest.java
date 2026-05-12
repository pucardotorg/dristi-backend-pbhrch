package org.pucar.dristi.caselifecycle.hearing.internal.web.models.taskManagement;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.egov.common.contract.request.RequestInfo;
import org.pucar.dristi.caselifecycle.hearing.internal.web.models.Pagination;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskSearchRequest {

    @JsonProperty("RequestInfo")
    private RequestInfo requestInfo;

    @JsonProperty("criteria")
    private TaskSearchCriteria criteria;

    @JsonProperty("pagination")
    private Pagination pagination;
}
