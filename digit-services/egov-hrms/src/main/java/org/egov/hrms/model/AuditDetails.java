package org.egov.hrms.model;

import lombok.*;

import javax.validation.constraints.NotNull;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;


@AllArgsConstructor
@Builder
@Getter
@NoArgsConstructor
@Setter
@ToString
public class AuditDetails {

    private String createdBy;

    private OffsetDateTime createdDate;

    private String lastModifiedBy;

    private OffsetDateTime lastModifiedDate;


}