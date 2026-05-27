// HAND-CURATED — lifted by Phase 35 (contract-lift)
package org.pucar.dristi.common.contract.ordermanagement;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;


@Data
public class HearingDraftOrder {

    @JsonProperty("tenantId")
    private String tenantId = null;

    @JsonProperty("filingNumber")
    private String filingNumber = null;

    @JsonProperty("cnrNumber")
    private String cnrNumber = null;

    @JsonProperty("hearingNumber")
    private String hearingNumber = null;

    @JsonProperty("hearingType")
    private String hearingType = null;
}
