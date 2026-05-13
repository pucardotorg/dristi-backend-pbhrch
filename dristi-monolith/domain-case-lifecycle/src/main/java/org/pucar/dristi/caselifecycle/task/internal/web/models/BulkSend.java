package org.pucar.dristi.caselifecycle.task.internal.web.models;

import lombok.Data;

@Data
public class BulkSend {

    private String tenantId;
    private String taskNumber;
    private boolean isSuccess = true;
    private String errorMessage;
}
