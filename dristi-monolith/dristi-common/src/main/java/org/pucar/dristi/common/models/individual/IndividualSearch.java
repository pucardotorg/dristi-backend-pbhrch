// HAND-CURATED — extracted from per-service copies
package org.pucar.dristi.common.models.individual;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class IndividualSearch {

    @JsonProperty("id")
    private List<String> id;

    @JsonProperty("individualId")
    private String individualId;

    @JsonProperty("clientReferenceId")
    private List<String> clientReferenceId;

    @JsonProperty("dateOfBirth")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
    private Date dateOfBirth;

    @JsonProperty("mobileNumber")
    private String mobileNumber;

    @JsonProperty("wardCode")
    private String wardCode;

    @JsonProperty("individualName")
    private String individualName;

    @JsonProperty("createdFrom")
    private BigDecimal createdFrom;

    @JsonProperty("createdTo")
    private BigDecimal createdTo;

    @JsonProperty("boundaryCode")
    private String boundaryCode;

    @JsonProperty("roleCodes")
    private List<String> roleCodes;

    @JsonProperty("username")
    private String username;

    @JsonProperty("userId")
    private Long userId;

    @JsonProperty("userUuid")
    @Size(min = 1)
    private List<String> userUuid;

    @JsonProperty("latitude")
    @DecimalMin("-90")
    @DecimalMax("90")
    private Double latitude;

    @JsonProperty("longitude")
    @DecimalMin("-180")
    @DecimalMax("180")
    private Double longitude;

    /** Unit of measurement: kilometres. */
    @JsonProperty("searchRadius")
    @DecimalMin("0")
    private Double searchRadius;
}
