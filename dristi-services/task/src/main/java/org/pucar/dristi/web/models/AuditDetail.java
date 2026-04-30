package org.pucar.dristi.web.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditDetail {

	private String createdBy;

	private String lastModifiedBy;

	private OffsetDateTime createdTime;

	private OffsetDateTime lastModifiedTime;
}
