package org.pucar.dristi.caselifecycle.task.internal.util;

import org.egov.common.contract.request.RequestInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pucar.dristi.caselifecycle.order.OrderApi;
import org.pucar.dristi.common.contract.order.Order;
import org.pucar.dristi.common.contract.order.OrderListResponse;
import org.pucar.dristi.common.contract.order.OrderSearchRequest;

import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OrderUtilTest {

    @InjectMocks
    private OrderUtil orderUtil;

    @Mock
    private OrderApi orderApi;

    private RequestInfo requestInfo;
    private UUID orderId;

    @BeforeEach
    void setUp() {
        requestInfo = new RequestInfo();
        orderId = UUID.randomUUID();
    }

    @Test
    void testFetchOrderDetails_Returns_false() {
        OrderListResponse response = OrderListResponse.builder().list(Collections.emptyList()).build();
        when(orderApi.search(any(OrderSearchRequest.class))).thenReturn(response);

        Boolean exists = orderUtil.fetchOrderDetails(requestInfo, orderId);

        assertFalse(exists);
        verify(orderApi).search(any(OrderSearchRequest.class));
    }

    @Test
    void testFetchOrderDetailsSuccess() {
        Order order = new Order();
        OrderListResponse response = OrderListResponse.builder().list(Collections.singletonList(order)).build();
        when(orderApi.search(any(OrderSearchRequest.class))).thenReturn(response);

        Boolean exists = orderUtil.fetchOrderDetails(requestInfo, orderId);

        assertTrue(exists);
        verify(orderApi).search(any(OrderSearchRequest.class));
    }
}
