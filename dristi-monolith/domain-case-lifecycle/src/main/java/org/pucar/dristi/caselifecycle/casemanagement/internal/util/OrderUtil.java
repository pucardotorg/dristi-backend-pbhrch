package org.pucar.dristi.caselifecycle.casemanagement.internal.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.pucar.dristi.caselifecycle.casemanagement.internal.web.models.order.Order;
import org.pucar.dristi.caselifecycle.casemanagement.internal.web.models.order.OrderCriteria;
import org.pucar.dristi.caselifecycle.casemanagement.internal.web.models.order.OrderSearchRequest;
import org.pucar.dristi.common.order.OrderApi;
import org.pucar.dristi.common.contract.casemanagement.OrderPagination;
import org.pucar.dristi.common.contract.casemanagement.Pagination;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

import static org.pucar.dristi.caselifecycle.casemanagement.internal.config.ServiceConstants.ERROR_WHILE_FETCHING_FROM_ORDER;


@Component("casemanagementOrderUtil")
@Slf4j
public class OrderUtil {

    private final OrderApi orderApi;
    private final ObjectMapper objectMapper;

    public OrderUtil(ObjectMapper objectMapper, OrderApi orderApi) {
        this.orderApi = orderApi;
        this.objectMapper = objectMapper;
    }

    public List<Order> getOrders(String filingNumber, String courtId, RequestInfo requestInfo) {
        OrderSearchRequest localRequest = OrderSearchRequest.builder()
                .requestInfo(requestInfo)
                .criteria(OrderCriteria.builder().filingNumber(filingNumber).status("PUBLISHED").courtId(courtId).build())
                .pagination(Pagination.builder().sortBy("createdDate").order(OrderPagination.ASC).limit(100).build())
                .build();
        try {
            org.pucar.dristi.common.contract.order.OrderSearchRequest orderRequest =
                    objectMapper.convertValue(localRequest, org.pucar.dristi.common.contract.order.OrderSearchRequest.class);
            org.pucar.dristi.common.contract.order.OrderListResponse response = orderApi.search(orderRequest);
            if (response == null || response.getList() == null || response.getList().isEmpty()) {
                return Collections.emptyList();
            }
            return objectMapper.convertValue(
                    response.getList(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Order.class));
        } catch (Exception e) {
            log.error(ERROR_WHILE_FETCHING_FROM_ORDER, e);
            throw new CustomException(ERROR_WHILE_FETCHING_FROM_ORDER, e.getMessage());
        }
    }
}
