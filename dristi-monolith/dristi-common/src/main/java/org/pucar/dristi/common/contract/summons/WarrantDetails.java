// HAND-CURATED — lifted by Phase 35 (contract-lift)
package org.pucar.dristi.common.contract.summons;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

@Validated
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WarrantDetails {

    @JsonProperty("warrantId")
    private String warrantId = null;

    @JsonProperty("issueDate")
    private Long issueDate;

    @JsonProperty("docType")
    private String docType;

    @JsonProperty("docSubType")
    private String docSubType;

    @JsonProperty("partyType")
    private String partyType;

    @JsonProperty("bailableAmount")
    private String bailableAmount;

    @JsonProperty("surety")
    private Integer surety;

    @JsonProperty("executorName")
    private String executorName;

    @JsonProperty("templateType")
    private String templateType;

    @JsonProperty("warrantText")
    private String warrantText;
}
