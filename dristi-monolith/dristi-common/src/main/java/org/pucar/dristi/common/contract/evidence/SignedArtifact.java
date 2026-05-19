// HAND-CURATED — lifted by Phase 35 (contract-lift)
package org.pucar.dristi.common.contract.evidence;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SignedArtifact {
    @JsonProperty("artifactNumber")
    @NotNull
    private String artifactNumber;

    @JsonProperty("signedArtifactData")
    @NotNull
    private String signedArtifactData;

    @JsonProperty("isWitnessDeposition")
    private Boolean isWitnessDeposition;

    @JsonProperty("signed")
    @NotNull
    private Boolean signed;

    @JsonProperty("tenantId")
    private String tenantId;

    @JsonProperty("errorMsg")
    private String errorMsg;
}
