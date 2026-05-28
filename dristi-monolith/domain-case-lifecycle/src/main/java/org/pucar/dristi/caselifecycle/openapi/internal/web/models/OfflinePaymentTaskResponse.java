// Reverted from Phase 35 lift: references subpackage classes (Order, IssuedBy,
// StatuteSection, PartyDetails, OrderBy, OfflinePaymentTask) that remain under
// internal/web/models/<subpkg>/. Kept service-local since openapi is a leaf
// consumer with no peer-subdomain callers.
package org.pucar.dristi.caselifecycle.openapi.internal.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.egov.common.contract.response.ResponseInfo;
import org.pucar.dristi.caselifecycle.openapi.internal.web.models.offline_payments.OfflinePaymentTask;
import org.springframework.validation.annotation.Validated;

@Validated
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OfflinePaymentTaskResponse {

    @JsonProperty("responseInfo")
    private ResponseInfo responseInfo;

    @JsonProperty("offlinePaymentTask")
    private OfflinePaymentTask offlinePaymentTask;
    
}
