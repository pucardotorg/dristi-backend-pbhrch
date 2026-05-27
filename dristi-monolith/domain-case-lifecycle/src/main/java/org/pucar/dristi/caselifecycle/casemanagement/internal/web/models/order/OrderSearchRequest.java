package org.pucar.dristi.caselifecycle.casemanagement.internal.web.models.order;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import lombok.*;
import org.egov.common.contract.request.RequestInfo;
import org.pucar.dristi.common.contract.casemanagement.Pagination;
import org.springframework.validation.annotation.Validated;

import org.pucar.dristi.common.contract.casemanagement.CaseSearchRequest;
/**
 * CaseSearchRequest
 */
@Validated
@jakarta.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2024-04-15T11:31:40.281899+05:30[Asia/Kolkata]")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderSearchRequest {

    @JsonProperty("RequestInfo")
    @Valid
    private RequestInfo requestInfo = null;

    @JsonProperty("criteria")
    @Valid
    private OrderCriteria criteria = null;

    @JsonProperty("pagination")
    private Pagination pagination = null;

}