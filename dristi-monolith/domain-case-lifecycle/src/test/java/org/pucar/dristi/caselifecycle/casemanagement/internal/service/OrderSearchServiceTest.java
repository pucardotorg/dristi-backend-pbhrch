package org.pucar.dristi.caselifecycle.casemanagement.internal.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderSearchServiceTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private OrderApi orderApi;

    @InjectMocks
    private OrderSearchService orderSearchService;

    @Test
    void testSearchOrderSuccess() {
        RequestInfo requestInfo = new RequestInfo();
        requestInfo.setAuthToken("auth-token");

        Order order = new Order();
        order.setCnrNumber("CNR123");
        OrderListResponse response = new OrderListResponse();
        response.setList(List.of(order));
        when(orderApi.search(any(OrderSearchRequest.class))).thenReturn(response);

        String cnrNumber = orderSearchService.searchOrder("123", "tenant1", requestInfo);

        assertEquals("CNR123", cnrNumber);
        verify(orderApi, times(1)).search(any(OrderSearchRequest.class));
    }

    @Test
    void testSearchOrderApiException() {
        RequestInfo requestInfo = new RequestInfo();
        requestInfo.setAuthToken("auth-token");

        when(orderApi.search(any(OrderSearchRequest.class)))
                .thenThrow(new RuntimeException("OrderApi Exception"));

        CustomException exception = assertThrows(CustomException.class, () ->
                orderSearchService.searchOrder("123", "tenant1", requestInfo));

        assertEquals("ORDER_SEARCH_ERR", exception.getCode());
        assertTrue(exception.getMessage().contains("Error while fetching the order details"));
    }

    @Test
    void testSearchOrderEmptyList() {
        RequestInfo requestInfo = new RequestInfo();
        requestInfo.setAuthToken("auth-token");

        OrderListResponse response = new OrderListResponse();
        response.setList(Collections.emptyList());
        when(orderApi.search(any(OrderSearchRequest.class))).thenReturn(response);

        CustomException exception = assertThrows(CustomException.class, () ->
                orderSearchService.searchOrder("123", "tenant1", requestInfo));

        assertEquals("ORDER_SEARCH_ERR", exception.getCode());
        assertTrue(exception.getMessage().contains("Response body is null"));
    }

    @Test
    void testSearchOrderResponseNull() {
        RequestInfo requestInfo = new RequestInfo();
        requestInfo.setAuthToken("auth-token");

        when(orderApi.search(any(OrderSearchRequest.class))).thenReturn(null);

        CustomException exception = assertThrows(CustomException.class, () ->
                orderSearchService.searchOrder("123", "tenant1", requestInfo));

        assertEquals("ORDER_SEARCH_ERR", exception.getCode());
        assertTrue(exception.getMessage().contains("Response body is null"));
    }
}
