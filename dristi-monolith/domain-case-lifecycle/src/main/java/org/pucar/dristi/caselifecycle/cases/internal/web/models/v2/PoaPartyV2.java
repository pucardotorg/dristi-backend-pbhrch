package org.pucar.dristi.caselifecycle.cases.internal.web.models.v2;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PoaPartyV2 {

    @JsonProperty("individualId")
    private String individualId;
}
