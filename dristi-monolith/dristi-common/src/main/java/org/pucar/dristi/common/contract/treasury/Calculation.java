package org.pucar.dristi.common.contract.treasury;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Calculation {

    @JsonProperty("applicationId")
    private String applicationId;

    @JsonProperty("tenantId")
    private String tenantId;

    @JsonProperty("totalAmount")
    private Double totalAmount;

    @JsonProperty("breakDown")
    private List<BreakDown> breakDown;
}
