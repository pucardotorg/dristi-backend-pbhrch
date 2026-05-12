package org.pucar.dristi.caselifecycle.hearing.internal.web.models.taskManagement;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.egov.common.contract.response.ResponseInfo;
import org.pucar.dristi.caselifecycle.hearing.internal.web.models.Pagination;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskManagementSearchResponse {

    @JsonProperty("ResponseInfo")
    private ResponseInfo responseInfo;

    @JsonProperty("taskManagementRecords")
    private List<TaskManagement> taskManagementRecords;

    @JsonProperty("pagination")
    private Pagination pagination;

    @JsonProperty("totalCount")
    private Integer totalCount;
}
