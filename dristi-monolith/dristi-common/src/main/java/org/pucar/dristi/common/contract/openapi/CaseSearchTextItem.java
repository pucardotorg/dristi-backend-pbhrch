// HAND-CURATED — lifted by Phase 35 (contract-lift)
package org.pucar.dristi.common.contract.openapi;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class CaseSearchTextItem {

    @JsonProperty("cmpNumber")
    private String cmpNumber;

    @JsonProperty("filingNumber")
    private String filingNumber;

    @JsonProperty("courtCaseNumber")
    private String courtCaseNumber;

    @JsonProperty("cnrNumber")
    private String cnrNumber;

    @JsonProperty("caseTitle")
    private String caseTitle;
}
