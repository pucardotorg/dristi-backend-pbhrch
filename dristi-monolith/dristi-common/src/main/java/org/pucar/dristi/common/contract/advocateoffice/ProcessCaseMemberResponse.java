// HAND-CURATED — lifted by Phase 35 (contract-lift)
package org.pucar.dristi.common.contract.advocateoffice;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.egov.common.contract.response.ResponseInfo;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcessCaseMemberResponse {

    @JsonProperty("ResponseInfo")
    private ResponseInfo responseInfo;

    @JsonProperty("processCaseMember")
    private ProcessCaseMember processCaseMember;
}
