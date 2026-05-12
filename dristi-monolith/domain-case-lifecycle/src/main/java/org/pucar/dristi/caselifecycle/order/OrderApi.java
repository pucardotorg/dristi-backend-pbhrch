package org.pucar.dristi.caselifecycle.order;

import org.pucar.dristi.common.contract.order.OrderListResponse;
import org.pucar.dristi.common.contract.order.OrderSearchRequest;

/**
 * Public, cross-subdomain API of the order subdomain. Other modules
 * consume order through this interface — never by importing from
 * {@code internal/}.
 *
 * <p>No caller exists in the migrated tree today (cases predates order
 * conceptually and only declares but never invokes order helpers).
 * The scaffold is in place for the imminent migrations of hearing,
 * task, evidence and similar services that legitimately call order:
 * they should add the methods they need here rather than re-deriving a
 * REST helper.
 *
 * <p>Order's contract DTOs already live at
 * {@code dristi-common/contract/order/} (lifted by Phase 35 during
 * the order migration), so this API consumes the canonical types
 * directly.
 */
public interface OrderApi {

    /**
     * Search orders matching the given criteria + pagination.
     * Returns the same {@link OrderListResponse} envelope the HTTP
     * controller would have returned, so callers swapping from REST
     * keep the same downstream parsing logic.
     */
    OrderListResponse search(OrderSearchRequest request);
}
