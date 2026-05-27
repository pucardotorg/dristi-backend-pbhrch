package org.pucar.dristi.caselifecycle.analytics.internal.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.egov.common.contract.request.RequestInfo;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pucar.dristi.common.order.OrderApi;
import org.pucar.dristi.common.contract.order.Order;
import org.pucar.dristi.common.contract.order.OrderListResponse;
import org.pucar.dristi.common.contract.order.OrderSearchRequest;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderUtilTest {

    @Mock
    private OrderApi orderApi;

    @Mock
    private ObjectMapper mapper;

    @InjectMocks
    private OrderUtil orderUtil;

    @Test
    void testGetOrder_Success() throws Exception {
        JSONObject request = new JSONObject().put("RequestInfo", new JSONObject());
        RequestInfo requestInfo = RequestInfo.builder().build();
        Order order = new Order();
        OrderListResponse response = new OrderListResponse();
        response.setList(List.of(order));
        com.fasterxml.jackson.databind.node.ObjectNode orderNode =
                new ObjectMapper().createObjectNode().put("id", "order-123");

        when(mapper.convertValue(any(), eq(RequestInfo.class))).thenReturn(requestInfo);
        when(orderApi.search(any(OrderSearchRequest.class))).thenReturn(response);
        when(mapper.valueToTree(order)).thenReturn(orderNode);

        Object result = orderUtil.getOrder(request, "order-123", "tenant1");
        assertNotNull(result);
        assertInstanceOf(JSONObject.class, result);
        assertEquals("order-123", ((JSONObject) result).getString("id"));
    }

    @Test
    void testGetOrder_NoOrders() throws Exception {
        JSONObject request = new JSONObject().put("RequestInfo", new JSONObject());
        OrderListResponse response = new OrderListResponse();
        response.setList(Collections.emptyList());

        when(mapper.convertValue(any(), eq(RequestInfo.class))).thenReturn(RequestInfo.builder().build());
        when(orderApi.search(any(OrderSearchRequest.class))).thenReturn(response);

        assertNull(orderUtil.getOrder(request, "order-999", "tenant1"));
    }

    @Test
    void testGetOrder_Exception() throws Exception {
        JSONObject request = new JSONObject().put("RequestInfo", new JSONObject());
        when(mapper.convertValue(any(), eq(RequestInfo.class))).thenReturn(RequestInfo.builder().build());
        when(orderApi.search(any())).thenThrow(new RuntimeException("upstream"));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> orderUtil.getOrder(request, "order-123", "tenant1"));
        assertEquals("Error while fetching or processing the order response", ex.getMessage());
    }
}
