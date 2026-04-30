package org.egov.transformer.models.digitalized_document;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Details captured for mediation proceedings.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MediationDetails {

    @JsonProperty("natureOfComplainant")
    private String natureOfComplainant = null;

    @JsonProperty("dateOfInstitution")
    private OffsetDateTime dateOfInstitution = null;

    @JsonProperty("caseStage")
    private String caseStage = null;

    @JsonProperty("pdfCreatedDate")
    private OffsetDateTime pdfCreatedDate = null;

    @JsonProperty("hearingDate")
    private OffsetDateTime hearingDate = null;

    @JsonProperty("dateOfEndADR")
    private OffsetDateTime dateOfEndADR = null;

    @JsonProperty("mediationCentre")
    private String mediationCentre = null;

    @JsonProperty("partyDetails")
    @Valid
    private List<MediationPartyDetails> partyDetails = null;


    public MediationDetails addPartyDetailsItem(MediationPartyDetails partyDetailsItem) {
        if (this.partyDetails == null) {
            this.partyDetails = new ArrayList<>();
        }
        this.partyDetails.add(partyDetailsItem);
        return this;
    }

}
