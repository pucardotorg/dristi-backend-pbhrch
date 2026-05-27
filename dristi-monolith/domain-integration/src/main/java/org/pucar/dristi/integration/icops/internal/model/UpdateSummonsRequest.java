package org.pucar.dristi.integration.icops.internal.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import org.egov.common.contract.request.RequestInfo;

@Data
@Builder
public class UpdateSummonsRequest {

    @JsonProperty("RequestInfo")
    private RequestInfo requestInfo;

    @JsonProperty("ChannelReport")
    private ChannelReport channelReport;
}
