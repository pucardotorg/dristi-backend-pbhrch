package org.egov.userevent.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.OffsetDateTime;
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

    private OffsetDateTime createdTime;

    private String lastModifiedBy;

    private OffsetDateTime lastModifiedTime;
}