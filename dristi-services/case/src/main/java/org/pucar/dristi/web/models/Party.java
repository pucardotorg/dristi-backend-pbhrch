package org.pucar.dristi.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.egov.common.contract.models.AuditDetails;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.UUID;

/**
 * Party
 */
@Validated
@jakarta.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2024-04-15T11:31:40.281899+05:30[Asia/Kolkata]")
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class Party {
    @JsonProperty("id")

    @Valid
    private UUID id = null;

    @JsonProperty("tenantId")
    //@NotNull

    private String tenantId = null;

    @JsonProperty("caseId")

    private String caseId = null;

    @JsonProperty("partyCategory")
    //@NotNull

    private String partyCategory = null;

    @JsonProperty("organisationID")

    private String organisationID = null;

    @JsonProperty("individualId")

    private String individualId = null;

    @JsonProperty("partyType")

    private String partyType = null;

    @JsonProperty("isActive")

    private Boolean isActive = true;

    @JsonProperty("isResponseRequired")

    private Boolean isResponseRequired = false;

    @JsonProperty("isPartyInPerson")
    private boolean isPartyInPerson;

    @JsonProperty("firstName")
    private String firstName = null;

    @JsonProperty("middleName")
    private String middleName = null;

    @JsonProperty("lastName")
    private String lastName = null;

    @JsonProperty("fullName")
    private String fullName = null;

    @JsonProperty("mobileNumber")
    private String mobileNumber = null;

    @JsonProperty("age")
    private String age = null;

    @JsonProperty("complainantType")
    @Valid
    private ComplainantTypeInfo complainantType = null;

    @JsonProperty("complainantTypeOfEntity")
    @Valid
    private ComplainantTypeOfEntity complainantTypeOfEntity = null;

    @JsonProperty("companyName")
    private String companyName = null;

    @JsonProperty("designation")
    private String designation = null;

    @JsonProperty("transferredPOA")
    @Valid
    private TransferredPOAInfo transferredPOA = null;

    @JsonProperty("permanentAddress")
    @Valid
    private Address permanentAddress = null;

    @JsonProperty("currentAddress")
    @Valid
    private Address currentAddress = null;

    @JsonProperty("companyAddress")
    @Valid
    private Address companyAddress = null;

    @JsonProperty("documents")
    @Valid
    private List<Document> documents = null;

    @JsonProperty("auditDetails")

    @Valid
    private AuditDetails auditDetails = null;

    @JsonProperty("additionalDetails")

    private Object additionalDetails = null;

    @JsonProperty("hasSigned")
    private Boolean hasSigned = false;


}
