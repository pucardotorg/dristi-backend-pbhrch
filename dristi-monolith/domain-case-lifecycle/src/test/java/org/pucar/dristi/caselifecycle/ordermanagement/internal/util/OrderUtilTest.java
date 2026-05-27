package org.pucar.dristi.caselifecycle.ordermanagement.internal.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.egov.tracer.model.CustomException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pucar.dristi.caselifecycle.order.OrderApi;
import org.pucar.dristi.caselifecycle.ordermanagement.internal.config.Configuration;
import org.pucar.dristi.common.contract.ordermanagement.OrderExistsRequest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderUtilTest {

    @Mock
    private Configuration configuration;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private OrderApi orderApi;

    @Mock
    private LocalizationUtil localizationUtil;

    @InjectMocks
    private OrderUtil orderUtil;

    @Test
    void testFetchOrderDetails_Exception() {
        OrderExistsRequest request = new OrderExistsRequest();
        when(orderApi.exists(any(org.pucar.dristi.common.contract.order.OrderExistsRequest.class)))
                .thenThrow(new RuntimeException("Service error"));

        assertThrows(CustomException.class, () -> orderUtil.fetchOrderDetails(request));
    }
}
