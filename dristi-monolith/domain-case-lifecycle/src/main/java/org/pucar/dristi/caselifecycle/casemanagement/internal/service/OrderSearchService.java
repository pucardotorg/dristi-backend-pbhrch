package org.pucar.dristi.caselifecycle.casemanagement.internal.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.pucar.dristi.common.order.OrderApi;
import org.pucar.dristi.common.contract.order.Order;
import org.pucar.dristi.common.contract.order.OrderCriteria;
import org.pucar.dristi.common.contract.order.OrderListResponse;
import org.pucar.dristi.common.contract.order.OrderSearchRequest;
import org.pucar.dristi.common.contract.order.Pagination;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class OrderSearchService {

    private final ObjectMapper objectMapper;
    private final OrderApi orderApi;

    @Autowired
    public OrderSearchService(ObjectMapper objectMapper, OrderApi orderApi) {
        this.objectMapper = objectMapper;
        this.orderApi = orderApi;
    }

    public String searchOrder(String referenceId, String tenantId, RequestInfo requestInfo)  {
        OrderSearchRequest request = new OrderSearchRequest();
        request.setRequestInfo(requestInfo);
        OrderCriteria criteria = new OrderCriteria();
        criteria.setId(referenceId);
        criteria.setTenantId(tenantId);
        request.setCriteria(criteria);
        Pagination pagination = new Pagination();
        pagination.setLimit(1.0);
        pagination.setOffSet(0.0);
        request.setPagination(pagination);

        OrderListResponse response;
        try {
            response = orderApi.search(request);
        } catch (Exception e) {
            throw new CustomException("ORDER_SEARCH_ERR", "Error while fetching the order details: " + e.getMessage());
        }

        if (response == null || response.getList() == null || response.getList().isEmpty()) {
            throw new CustomException("ORDER_SEARCH_ERR", "Response body is null");
        }

        String cnrNumber;
        try {
            Order first = response.getList().get(0);
            cnrNumber = first.getCnrNumber();
            log.info("Response from the order search :: list size={}", response.getList().size());
        } catch (Exception e) {
            throw new CustomException("JSON_PARSING_ERROR", "Error while extracting cnr number from the order response");
        }
        log.info("CNR Number: {}", cnrNumber);
        return cnrNumber;
    }
}
