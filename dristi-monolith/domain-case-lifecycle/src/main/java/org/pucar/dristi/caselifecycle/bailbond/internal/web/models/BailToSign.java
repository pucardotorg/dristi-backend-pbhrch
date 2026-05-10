package org.pucar.dristi.caselifecycle.bailbond.internal.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

@Validated
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BailToSign {
    @JsonProperty("bailId")
    private String bailId = null;

    @JsonProperty("request")
    private String request;
}
