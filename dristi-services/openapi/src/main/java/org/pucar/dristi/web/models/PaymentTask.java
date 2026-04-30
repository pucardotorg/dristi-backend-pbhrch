package org.pucar.dristi.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class PaymentTask {
    @JsonProperty("dueDate")
    private OffsetDateTime dueDate;

    @JsonProperty("daysRemaining")
    private Integer daysRemaining;

    @JsonProperty("task")
    private String task;
}
