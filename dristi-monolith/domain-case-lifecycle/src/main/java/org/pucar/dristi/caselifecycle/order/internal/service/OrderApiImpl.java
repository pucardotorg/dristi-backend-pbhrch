package org.pucar.dristi.caselifecycle.order.internal.service;

import java.util.List;

import org.egov.common.contract.response.ResponseInfo;
import org.pucar.dristi.caselifecycle.order.OrderApi;
import org.pucar.dristi.common.contract.order.Order;
import org.pucar.dristi.common.contract.order.OrderListResponse;
import org.pucar.dristi.common.contract.order.OrderSearchRequest;
import org.pucar.dristi.common.util.ResponseInfoFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class OrderApiImpl implements OrderApi {

    private final OrderRegistrationService orderRegistrationService;
    private final ResponseInfoFactory responseInfoFactory;

    @Autowired
    public OrderApiImpl(OrderRegistrationService orderRegistrationService,
                        ResponseInfoFactory responseInfoFactory) {
        this.orderRegistrationService = orderRegistrationService;
        this.responseInfoFactory = responseInfoFactory;
    }

    @Override
    public OrderListResponse search(OrderSearchRequest request) {
        List<Order> orders = orderRegistrationService.searchOrder(request);
        ResponseInfo responseInfo = responseInfoFactory.createResponseInfoFromRequestInfo(
                request.getRequestInfo(), true, HttpStatus.OK.getReasonPhrase());
        int totalCount = (request.getPagination() != null && request.getPagination().getTotalCount() != null)
                ? request.getPagination().getTotalCount().intValue()
                : orders.size();
        return OrderListResponse.builder()
                .list(orders)
                .totalCount(totalCount)
                .pagination(request.getPagination())
                .responseInfo(responseInfo)
                .build();
    }
}
