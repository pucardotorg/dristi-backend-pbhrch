// HAND-CURATED — lifted by Phase 35 (contract-lift)
package org.pucar.dristi.common.contract.openapi;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import lombok.*;
import org.egov.common.contract.response.ResponseInfo;

import java.util.List;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CaseSearchTextResponse {

    @JsonProperty("responseInfo")
    @Valid
    private ResponseInfo responseInfo;

    @JsonProperty("cases")
    @Valid
    private List<CaseSearchTextItem> cases;

    @JsonProperty("pagination")
    @Valid
    private Pagination pagination;
}
