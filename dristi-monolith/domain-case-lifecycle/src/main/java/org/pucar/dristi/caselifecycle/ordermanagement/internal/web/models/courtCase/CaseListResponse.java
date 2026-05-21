package org.pucar.dristi.caselifecycle.ordermanagement.internal.web.models.courtCase;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.egov.common.contract.response.ResponseInfo;
import org.pucar.dristi.common.contract.ordermanagement.Pagination;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CaseListResponse {

    @JsonProperty("ResponseInfo")
    @Valid
    private ResponseInfo responseInfo = null;

    @JsonProperty("criteria")
    @Valid
    private List<CaseCriteria> criteria = null;

    @JsonProperty("pagination")

    @Valid
    private Pagination pagination = null;

}
