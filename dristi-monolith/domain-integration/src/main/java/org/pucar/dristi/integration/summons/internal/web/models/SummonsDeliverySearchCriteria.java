package org.pucar.dristi.integration.summons.internal.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class SummonsDeliverySearchCriteria {

    @JsonProperty("summonsDeliveryId")
    private String summonsDeliveryId = null;

    @JsonProperty("taskNumber")
    private String taskNumber = null;
}