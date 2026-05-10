// HAND-CURATED — lifted by Phase 35 (contract-lift)
package org.pucar.dristi.common.contract.bailbond;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.Set;

@AllArgsConstructor
@Getter
@Builder
@EqualsAndHashCode
@NoArgsConstructor
public class Email {

    private Set<String> emailTo;
    private String subject;
    private String body;
    private String tenantId;
    private Map<String, String> fileStoreId;
    private String templateCode;
    private Boolean isHTML;
}

