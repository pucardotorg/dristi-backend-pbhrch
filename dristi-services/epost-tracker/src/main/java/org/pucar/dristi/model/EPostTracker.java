package org.pucar.dristi.model;

import lombok.*;
import org.egov.common.contract.models.AuditDetails;

import java.time.OffsetDateTime;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EPostTracker {
    private String processNumber;
    private String tenantId;
    private String taskType;
    private String respondentName;
    private String taskNumber;
    private String fileStoreId;
    private String trackingNumber;
    private String address;
    private Address addressObj;
    private String phone;
    private String pinCode;
    private DeliveryStatus deliveryStatus;
    private String remarks;
    private Object additionalDetails;
    private Integer rowVersion;
    private OffsetDateTime bookingDate;
    private OffsetDateTime receivedDate;
    private OffsetDateTime statusUpdateDate;
    private String postalHub;
    private AuditDetails auditDetails;
    private String totalAmount;
    private String speedPostId;
}
