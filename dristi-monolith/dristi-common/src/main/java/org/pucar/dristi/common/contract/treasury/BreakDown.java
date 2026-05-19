package org.pucar.dristi.common.contract.treasury;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BreakDown {

    private String type;
    private String code;
    private Double amount;
    private Map<String, Double> additionalParams;
}
