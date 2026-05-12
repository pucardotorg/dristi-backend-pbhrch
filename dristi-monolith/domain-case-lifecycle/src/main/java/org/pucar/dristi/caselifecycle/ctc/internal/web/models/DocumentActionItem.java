package org.pucar.dristi.caselifecycle.ctc.internal.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

import org.pucar.dristi.common.models.Document;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentActionItem {

    @JsonProperty("docId")
    @NotBlank
    private String docId;

    @JsonProperty("ctcApplicationNumber")
    @NotBlank
    private String ctcApplicationNumber;

    @JsonProperty("filingNumber")
    @NotBlank
    private String filingNumber;

    @JsonProperty("documents")
    private List<Document> documents;
}
