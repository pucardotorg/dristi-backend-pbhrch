package org.pucar.dristi.web.models.demand;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.pucar.dristi.web.models.demand.enums.Status;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CollectedReceipt {

    private String businessService;

    private String consumerCode;

    private String receiptNumber;

    private Double receiptAmount;

    private OffsetDateTime receiptDate;

    private Status status;

    private AuditDetail auditDetail;

    private String tenantId;
}
