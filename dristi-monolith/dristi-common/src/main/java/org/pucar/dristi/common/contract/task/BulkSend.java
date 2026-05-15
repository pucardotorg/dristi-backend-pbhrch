// HAND-CURATED — lifted by Phase 35 (contract-lift)
package org.pucar.dristi.common.contract.task;

import lombok.Data;

@Data
public class BulkSend {

    private String tenantId;
    private String taskNumber;
    private boolean isSuccess = true;
    private String errorMessage;
}
