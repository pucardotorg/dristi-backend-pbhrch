// HAND-CURATED — lifted by Phase 35 (contract-lift)
package org.pucar.dristi.common.contract.openapi;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LandingPageFileRequest {

    @JsonProperty("fileStoreId")
    @Valid
    @NotNull
    private String fileStoreId;

    @JsonProperty("tenantId")
    @Valid
    private String tenantId;

    @JsonProperty("moduleName")
    @Valid
    @NotNull
    private String moduleName;
}
