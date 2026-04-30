// HAND-CURATED — extracted from per-service copies (case/task/evidence are
// effectively identical; this version uses jakarta.validation for Spring
// Boot 3.x compatibility and the dristi-common AuditDetails).
package org.pucar.dristi.common.models.individual;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.egov.common.models.individual.AdditionalFields;
import org.egov.common.models.individual.Address;
import org.egov.common.models.individual.BloodGroup;
import org.egov.common.models.individual.Gender;
import org.egov.common.models.individual.Identifier;
import org.egov.common.models.individual.Name;
import org.egov.common.models.individual.Skill;
import org.egov.common.models.individual.UserDetails;
import org.pucar.dristi.common.models.AuditDetails;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * DRISTI Individual representation. Wraps the eGov platform's individual
 * sub-models (Name, Address, Identifier, Skill, etc. from
 * {@code org.egov.common.models.individual}) and adds DRISTI-specific
 * fields (userId / userUuid / system-user flags).
 */
@Schema(description = "A representation of an Individual.")
@Validated
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class Individual {

    @JsonProperty("id")
    @Size(min = 2, max = 64)
    private String id;

    @JsonProperty("individualId")
    @Size(min = 2, max = 64)
    private String individualId;

    @JsonProperty("tenantId")
    @NotNull
    @Size(min = 2, max = 1000)
    private String tenantId;

    @JsonProperty("clientReferenceId")
    @Size(min = 2, max = 64)
    private String clientReferenceId;

    @JsonProperty("userId")
    private String userId;

    @JsonProperty("userUuid")
    private String userUuid;

    @JsonProperty("name")
    @Valid
    private Name name;

    @JsonProperty("dateOfBirth")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
    private Date dateOfBirth;

    @JsonProperty("gender")
    @Valid
    private Gender gender;

    @JsonProperty("bloodGroup")
    @Valid
    private BloodGroup bloodGroup;

    @JsonProperty("mobileNumber")
    @Size(max = 20)
    private String mobileNumber;

    @JsonProperty("altContactNumber")
    @Size(max = 16)
    private String altContactNumber;

    @JsonProperty("email")
    @Size(min = 5, max = 200)
    private String email;

    @JsonProperty("address")
    @Valid
    @Size(max = 3)
    private List<Address> address;

    @JsonProperty("fatherName")
    @Size(max = 100)
    private String fatherName;

    @JsonProperty("husbandName")
    @Size(max = 100)
    private String husbandName;

    @JsonProperty("relationship")
    @Size(max = 100, min = 1)
    private String relationship;

    @JsonProperty("identifiers")
    @Valid
    private List<Identifier> identifiers;

    @JsonProperty("skills")
    @Valid
    private List<Skill> skills;

    @JsonProperty("photo")
    private String photo;

    @JsonProperty("additionalFields")
    @Valid
    private AdditionalFields additionalFields;

    @JsonProperty("isDeleted")
    private Boolean isDeleted = Boolean.FALSE;

    @JsonProperty("rowVersion")
    private Integer rowVersion;

    @JsonProperty("auditDetails")
    @Valid
    private AuditDetails auditDetails;

    @JsonProperty("clientAuditDetails")
    @Valid
    private AuditDetails clientAuditDetails;

    @JsonIgnore
    private Boolean hasErrors = Boolean.FALSE;

    @JsonProperty("isSystemUser")
    private Boolean isSystemUser = Boolean.FALSE;

    @JsonProperty("isSystemUserActive")
    private Boolean isSystemUserActive = Boolean.TRUE;

    @JsonProperty("userDetails")
    private UserDetails userDetails;


    public Individual addAddressItem(Address addressItem) {
        if (this.address == null) {
            this.address = new ArrayList<>();
        }
        this.address.add(addressItem);
        return this;
    }

    public Individual addIdentifiersItem(Identifier identifiersItem) {
        if (this.identifiers == null) {
            this.identifiers = new ArrayList<>();
        }
        this.identifiers.add(identifiersItem);
        return this;
    }

    public Individual addSkillsItem(Skill skillItem) {
        if (this.skills == null) {
            this.skills = new ArrayList<>();
        }
        this.skills.add(skillItem);
        return this;
    }
}
