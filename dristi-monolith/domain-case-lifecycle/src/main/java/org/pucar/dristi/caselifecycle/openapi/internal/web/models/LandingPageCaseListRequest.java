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
import org.pucar.dristi.caselifecycle.openapi.internal.web.models.inbox.OrderBy;
import org.pucar.dristi.common.contract.openapi.SearchCaseCriteria;
import org.pucar.dristi.common.contract.openapi.FilterCriteria;


import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LandingPageCaseListRequest {
    @JsonProperty("searchCaseCriteria")
    @Valid
    private SearchCaseCriteria searchCaseCriteria = null;

    @JsonProperty("filterCriteria")
    @Valid
    private FilterCriteria filterCriteria = null;

    @JsonProperty("offset")
    @Valid
    private Integer offset = null;

    @JsonProperty("limit")
    @Valid
    private Integer limit = null;

    @JsonProperty("sortOrder")
    @Valid
    private List<OrderBy> sortOrder = null;
}
