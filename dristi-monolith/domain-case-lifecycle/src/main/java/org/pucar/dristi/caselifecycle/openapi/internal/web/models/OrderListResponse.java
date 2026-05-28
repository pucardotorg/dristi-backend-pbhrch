// Reverted from Phase 35 lift: references subpackage classes (Order, IssuedBy,
// StatuteSection, PartyDetails, OrderBy, OfflinePaymentTask) that remain under
// internal/web/models/<subpkg>/. Kept service-local since openapi is a leaf
// consumer with no peer-subdomain callers.
package org.pucar.dristi.caselifecycle.openapi.internal.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.egov.common.contract.response.ResponseInfo;
import org.pucar.dristi.caselifecycle.openapi.internal.web.models.order.Order;
import org.pucar.dristi.common.contract.openapi.Pagination;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderListResponse {

    @JsonProperty("ResponseInfo")
    @Valid
    private ResponseInfo responseInfo = null;

    @JsonProperty("TotalCount")
    private Integer totalCount = null;

    @JsonProperty("list")
    @Valid
    private List<Order> list = null;

    @JsonProperty("pagination")
    @Valid
    private Pagination pagination = null;

}
