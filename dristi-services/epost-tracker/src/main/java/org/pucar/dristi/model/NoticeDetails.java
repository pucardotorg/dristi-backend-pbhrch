package org.pucar.dristi.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

import java.time.OffsetDateTime;

@Validated
@jakarta.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2024-05-29T13:38:04.562296+05:30[Asia/Calcutta]")
@Data
@Builder
public class NoticeDetails {

    @JsonProperty("noticeId")
    private String noticeId;

    @JsonProperty("issueDate")
    private OffsetDateTime issueDate;

    @JsonProperty("caseFilingDate")
    private OffsetDateTime caseFilingDate;

    @JsonProperty("docType")
    private String docType;

    @JsonProperty("docSubType")
    private String docSubType;

    @JsonProperty("partyType")
    private String partyType;

    @JsonProperty("noticeType")
    private String noticeType;
}
