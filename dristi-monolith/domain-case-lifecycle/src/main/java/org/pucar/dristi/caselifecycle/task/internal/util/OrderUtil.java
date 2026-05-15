package org.pucar.dristi.caselifecycle.task.internal.util;

import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.pucar.dristi.caselifecycle.order.OrderApi;
import org.pucar.dristi.common.contract.order.Order;
import org.pucar.dristi.common.contract.order.OrderCriteria;
import org.pucar.dristi.common.contract.order.OrderListResponse;
import org.pucar.dristi.common.contract.order.OrderSearchRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component("taskOrderUtil")
public class OrderUtil {

    private final OrderApi orderApi;

    @Autowired
    public OrderUtil(OrderApi orderApi) {
        this.orderApi = orderApi;
    }

    public Boolean fetchOrderDetails(RequestInfo requestInfo, UUID orderId) {
        OrderListResponse response = orderApi.search(buildSearchById(requestInfo, orderId.toString()));
        return response.getList() != null && !response.getList().isEmpty();
    }

    public Order getOrderByOrderId(RequestInfo requestInfo, String orderId) {
        OrderListResponse response = orderApi.search(buildSearchById(requestInfo, orderId));
        if (response.getList() == null || response.getList().isEmpty()) {
            throw new CustomException("ERROR_WHILE_FETCHING_FROM_ORDER",
                    "Order not found for order id: " + orderId);
        }
        return response.getList().get(0);
    }

    private static OrderSearchRequest buildSearchById(RequestInfo requestInfo, String orderId) {
        OrderCriteria criteria = new OrderCriteria();
        criteria.setId(orderId);
        OrderSearchRequest request = new OrderSearchRequest();
        request.setRequestInfo(requestInfo);
        request.setCriteria(criteria);
        return request;
    }
}
