package org.pucar.dristi.common.contract.epost;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.egov.common.contract.response.ResponseInfo;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChannelResponse {

    @JsonProperty("ResponseInfo")
    @Valid
    private ResponseInfo responseInfo = null;

    @JsonProperty("channelMessage")
    @Valid
    private ChannelMessage channelMessage = null;
}
