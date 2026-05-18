package org.pucar.dristi.caselifecycle.taskmanagement.internal.web.models.demand;

import org.pucar.dristi.caselifecycle.taskmanagement.internal.web.models.demand.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CollectedReceipt {

    private String businessService;

    private String consumerCode;

    private String receiptNumber;

    private Double receiptAmount;

    private Long receiptDate;

    private Status status;

    private AuditDetail auditDetail;

    private String tenantId;
}
