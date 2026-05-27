package org.pucar.dristi.common.order;

import org.pucar.dristi.common.contract.order.Order;
import org.pucar.dristi.common.contract.order.OrderExists;
import org.pucar.dristi.common.contract.order.OrderExistsRequest;
import org.pucar.dristi.common.contract.order.OrderListResponse;
import org.pucar.dristi.common.contract.order.OrderRequest;
import org.pucar.dristi.common.contract.order.OrderSearchRequest;
import org.pucar.dristi.common.contract.order.RemoveItemRequest;

import java.util.List;

/**
 * Public, cross-subdomain API of the order subdomain. Other modules
 * (order-management today) consume order through this interface —
 * never by importing from {@code internal/}.
 *
 * <p>Order's contract DTOs live at
 * {@code dristi-common/contract/order/} (lifted by Phase 35 during
 * the order migration), so this API consumes the canonical types
 * directly.
 */
public interface OrderApi {

    /**
     * Search orders matching the given criteria + pagination.
     * Returns the same {@link OrderListResponse} envelope the HTTP
     * controller would have returned.
     */
    OrderListResponse search(OrderSearchRequest request);

    /**
     * Existence check — mirrors {@code /order/v1/exists}. For each
     * criterion in the request, returns an {@link OrderExists} entry
     * indicating whether a matching order is present.
     */
    List<OrderExists> exists(OrderExistsRequest request);

    /**
     * Create a new order — mirrors {@code /order/v1/create}.
     */
    Order create(OrderRequest request);

    /**
     * Update an existing order (mainline workflow) — mirrors
     * {@code /order/v1/update}.
     */
    Order update(OrderRequest request);

    /**
     * Add an item to a composite order — mirrors {@code /order/v2/add-item}.
     */
    Order addOrderItem(OrderRequest request);

    /**
     * Remove an item from a composite order — mirrors
     * {@code /order/v2/remove-item}.
     */
    Order removeOrderItem(RemoveItemRequest request);
}
