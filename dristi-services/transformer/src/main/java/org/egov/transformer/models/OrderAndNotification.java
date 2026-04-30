package org.egov.transformer.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.egov.common.contract.models.Document;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderAndNotification {

    @JsonProperty("id")
    private String id;

    @JsonProperty("entityType")
    private String entityType;

    @JsonProperty("type")
    private String type;

    @JsonProperty("title")
    private String title;

    @JsonProperty("parties")
    private List<Map<String, Object>> parties;

    @JsonProperty("status")
    private String status;

    @JsonProperty("date")
    private OffsetDateTime date;

    @JsonProperty("tenantId")
    private String tenantId;

    @JsonProperty("filingNumbers")
    private List<String> filingNumbers;

    @JsonProperty("caseNumbers")
    private List<String> caseNumbers;

    @JsonProperty("courtId")
    private String courtId;

    @JsonProperty("businessOfTheDay")
    private String businessOfTheDay;

    @JsonProperty("judgeIds")
    private List<String> judgeIds;

    @JsonProperty("documents")
    private List<Document> documents;

    @JsonProperty("createdTime")
    private OffsetDateTime createdTime = null;

    @JsonProperty("lastModifiedTime")
    private OffsetDateTime lastModifiedTime = null;

    @JsonProperty("caseTitle")
    private String caseTitle = null;

    @JsonProperty("caseSTNumber")
    private String caseSTNumber = null;

}
