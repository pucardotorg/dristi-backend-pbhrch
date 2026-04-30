package org.pucar.dristi.web.models.billingservice;

import org.egov.common.contract.models.AuditDetails;

import java.time.OffsetDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;

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

	private AuditDetails auditDetail;

	private String tenantId;
}