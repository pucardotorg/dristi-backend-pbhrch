// HAND-CURATED — extracted as the union of Document fields across DRISTI services
package org.pucar.dristi.common.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DRISTI's Document model — used by FileStoreUtil's
 * {@code saveDocumentToFileStore} response and by every service that
 * tracks a single uploaded artifact.
 *
 * <p>Fields are the union of the per-service variants. Most services
 * carry id/documentType/documentUid/fileStore/additionalDetails/isActive;
 * documentName, tenantId, and auditDetails are present in subsets.
 * Including them all keeps the canonical compatible with every caller.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Document {

    @JsonProperty("id")
    private String id;

    @JsonProperty("documentType")
    private String documentType;

    @JsonProperty("fileName")
    private String fileName;

    @JsonProperty("documentName")
    private String documentName;

    @JsonProperty("fileStore")
    private String fileStore;

    @JsonProperty("documentUid")
    private String documentUid;

    @JsonProperty("isActive")
    private Boolean isActive = true;

    @JsonProperty("toDelete")
    private Boolean toDelete = false;

    @JsonProperty("tenantId")
    private String tenantId;

    @JsonProperty("additionalDetails")
    private Object additionalDetails;

    @JsonProperty("auditDetails")
    private AuditDetails auditDetails;
}
