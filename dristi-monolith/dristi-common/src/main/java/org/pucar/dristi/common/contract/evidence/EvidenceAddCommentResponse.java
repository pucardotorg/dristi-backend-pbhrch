// HAND-CURATED — lifted by Phase 35 (contract-lift)
package org.pucar.dristi.common.contract.evidence;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.egov.common.contract.response.ResponseInfo;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EvidenceAddCommentResponse {

    @JsonProperty("ResponseInfo")
    @Valid
    private ResponseInfo responseInfo = new ResponseInfo();

    @JsonProperty("evidenceAddComment")
    @Valid
    private EvidenceAddComment evidenceAddComment =  new EvidenceAddComment();;

}