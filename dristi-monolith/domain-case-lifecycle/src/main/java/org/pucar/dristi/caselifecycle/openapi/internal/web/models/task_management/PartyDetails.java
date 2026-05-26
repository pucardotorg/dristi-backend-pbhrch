package org.pucar.dristi.caselifecycle.openapi.internal.web.models.task_management;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.pucar.dristi.caselifecycle.openapi.internal.web.models.DeliveryChannel;
import org.pucar.dristi.caselifecycle.openapi.internal.web.models.address.PartyAddress;
import org.pucar.dristi.caselifecycle.openapi.internal.web.models.courtcase.RespondentDetails;
import org.pucar.dristi.caselifecycle.openapi.internal.web.models.courtcase.WitnessDetails;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PartyDetails {

    @JsonProperty("respondentDetails")
    private RespondentDetails respondentDetails;

    @JsonProperty("witnessDetails")
    private WitnessDetails witnessDetails;

    @JsonProperty("addresses")
    private List<PartyAddress> addresses;

    @JsonProperty("deliveryChannels")
    private List<DeliveryChannel> deliveryChannels;

}
