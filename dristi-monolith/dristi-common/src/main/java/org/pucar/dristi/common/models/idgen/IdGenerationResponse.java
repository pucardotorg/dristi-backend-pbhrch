// HAND-CURATED — do not regenerate
package org.pucar.dristi.common.models.idgen;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.egov.common.contract.response.ResponseInfo;

import java.util.List;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class IdGenerationResponse {
    private ResponseInfo responseInfo;
    private List<IdResponse> idResponses;
}
