// HAND-CURATED — lifted by Phase 35 (contract-lift)
package org.pucar.dristi.common.contract.esign;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SignDocParameter {

    @JsonProperty("response")
    private String response;

    @JsonProperty("txnId")
    private String txnId;

    @JsonProperty("tenantId")
    private String tenantId;
}
