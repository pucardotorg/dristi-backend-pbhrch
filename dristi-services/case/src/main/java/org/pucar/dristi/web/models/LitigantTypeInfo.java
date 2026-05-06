package org.pucar.dristi.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LitigantTypeInfo {

    @JsonProperty("id")
    private Integer id;

    @JsonProperty("code")
    private String code;

    @JsonProperty("name")
    private String name;

    @JsonProperty("isactive")
    private Boolean isactive;

    @JsonProperty("isIndividual")
    private Boolean isIndividual;

    @JsonProperty("commonFields")
    private Boolean commonFields;

    @JsonProperty("complainantTypeId")
    private Integer complainantTypeId;

    @JsonProperty("showCompanyDetails")
    private Boolean showCompanyDetails;

    @JsonProperty("complainantLocation")
    private Boolean complainantLocation;

}
