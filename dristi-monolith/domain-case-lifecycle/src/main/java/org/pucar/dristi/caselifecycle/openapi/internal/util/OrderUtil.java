package org.pucar.dristi.caselifecycle.openapi.internal.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.pucar.dristi.common.order.OrderApi;
import org.pucar.dristi.caselifecycle.openapi.internal.web.models.OrderListResponse;
import org.pucar.dristi.common.contract.openapi.OrderSearchRequest;
import org.springframework.stereotype.Component;

@Component("openapiOrderUtil")
@Slf4j
public class OrderUtil {

    private final OrderApi orderApi;
    private final ObjectMapper objectMapper;

    public OrderUtil(OrderApi orderApi, ObjectMapper objectMapper) {
        this.orderApi = orderApi;
        this.objectMapper = objectMapper;
    }

    public OrderListResponse getOrders(OrderSearchRequest searchRequest) {
        try {
            org.pucar.dristi.common.contract.order.OrderSearchRequest bridged =
                    objectMapper.convertValue(searchRequest,
                            org.pucar.dristi.common.contract.order.OrderSearchRequest.class);
            org.pucar.dristi.common.contract.order.OrderListResponse response = orderApi.search(bridged);
            return objectMapper.convertValue(response, OrderListResponse.class);
        } catch (Exception e) {
            log.error("Error fetching orders", e);
            return null;
        }
    }
}
