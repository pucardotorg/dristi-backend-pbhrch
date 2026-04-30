package org.pucar.dristi.web.models.task;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

import java.time.OffsetDateTime;

@Validated
@Data
@Builder
public class WarrantDetails {

    @JsonProperty("summonId")
    private String warrantId = null;

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

    @JsonProperty("surety")
    private String surety;

    @JsonProperty("bailableAmount")
    private Double bailableAmount;
}
