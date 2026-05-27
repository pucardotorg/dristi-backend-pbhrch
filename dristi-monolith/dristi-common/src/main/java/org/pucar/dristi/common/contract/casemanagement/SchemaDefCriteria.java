// HAND-CURATED — lifted by Phase 35 (contract-lift)
package org.pucar.dristi.common.contract.casemanagement;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.Set;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SchemaDefCriteria {

    @JsonProperty("tenantId")
    private String tenantId;

    @JsonProperty("codes")
    private Set<String> codes;
}
