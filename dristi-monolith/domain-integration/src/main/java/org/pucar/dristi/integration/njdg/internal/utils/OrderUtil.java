package org.pucar.dristi.integration.njdg.internal.utils;

import org.pucar.dristi.integration.njdg.internal.config.TransformerProperties;
import org.pucar.dristi.integration.njdg.internal.model.order.OrderListResponse;
import org.pucar.dristi.integration.njdg.internal.model.order.OrderSearchRequest;
import org.pucar.dristi.common.repository.ServiceRequestRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.tracer.model.CustomException;
import org.springframework.stereotype.Component;

import static org.pucar.dristi.integration.njdg.internal.config.ServiceConstants.ERROR_WHILE_FETCHING_FROM_ORDER;

@Component
@Slf4j
public class OrderUtil {

    private final TransformerProperties properties;

    private final ServiceRequestRepository serviceRequestRepository;
    private final ObjectMapper objectMapper;

    public OrderUtil(TransformerProperties properties, ServiceRequestRepository serviceRequestRepository, ObjectMapper objectMapper) {
        this.properties = properties;
        this.serviceRequestRepository = serviceRequestRepository;
        this.objectMapper = objectMapper;
    }

    public OrderListResponse getOrders(OrderSearchRequest searchRequest) {
        StringBuilder uri = new StringBuilder();
        uri.append(properties.getOrderHost()).append(properties.getOrderSearchEndPoint());
        Object response;
        OrderListResponse orderListResponse;
        try {
            response = serviceRequestRepository.fetchResult(uri, searchRequest);
            orderListResponse = objectMapper.convertValue(response, OrderListResponse.class);
        } catch (Exception e) {
            log.error(ERROR_WHILE_FETCHING_FROM_ORDER, e);
            throw new CustomException(ERROR_WHILE_FETCHING_FROM_ORDER, e.getMessage());

        }
        return orderListResponse;
    }
}
