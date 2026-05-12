package org.pucar.dristi.common.contract.epost;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.egov.common.contract.response.ResponseInfo;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EPostResponse {

    @JsonProperty("ResponseInfo")
    @Valid
    private ResponseInfo responseInfo = null;

    @JsonProperty("EPostTracker")
    @Valid
    private List<EPostTracker> ePostTrackers = null;

    @JsonProperty("pagination")
    @Valid
    private Pagination pagination = null;
}
