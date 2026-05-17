package org.pucar.dristi.integration.njdg.internal.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdvocateSerialNumber {
    private Integer serialNo;
    private UUID advocateId;
    private String barRegNo;
}
