// HAND-CURATED — lifted by Phase 35 (contract-lift)
package org.pucar.dristi.common.contract.analytics;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdvocateOffice {

    @JsonProperty("advocateOfficeName")
    private String advocateOfficeName;

    @JsonProperty("advocateUserUuid")
    private String advocateUserUuid;

    @JsonProperty("advocateId")
    private String advocateId;

    @JsonProperty("officeMembers")
    private List<String> officeMembers;

}
