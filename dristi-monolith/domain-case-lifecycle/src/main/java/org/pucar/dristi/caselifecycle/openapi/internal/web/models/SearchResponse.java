package org.pucar.dristi.caselifecycle.openapi.internal.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class SearchResponse {

    @JsonProperty("data")
    private List<Data> data;

}
