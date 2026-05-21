package org.pucar.dristi.caselifecycle.casemanagement.internal.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.egov.common.contract.request.RequestInfo;

import org.pucar.dristi.common.contract.casemanagement.SchemaDefCriteria;
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MdmsSearch {
    @JsonProperty("RequestInfo")
    private RequestInfo requestInfo;

    @JsonProperty("SchemaDefCriteria")
    private SchemaDefCriteria schemaDefCriteria;
}
