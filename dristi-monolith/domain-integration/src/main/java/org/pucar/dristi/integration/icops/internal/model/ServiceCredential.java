package org.pucar.dristi.integration.icops.internal.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ServiceCredential {
    private String serviceName;
    private String serviceKey;
}
