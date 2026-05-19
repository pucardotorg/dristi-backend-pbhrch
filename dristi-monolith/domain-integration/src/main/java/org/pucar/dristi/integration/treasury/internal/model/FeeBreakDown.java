package org.pucar.dristi.integration.treasury.internal.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FeeBreakDown {
    private String feeName;
    private double feeAmount;
}

