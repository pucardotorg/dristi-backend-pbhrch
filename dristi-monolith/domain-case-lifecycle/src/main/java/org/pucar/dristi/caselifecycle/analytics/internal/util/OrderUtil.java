package org.pucar.dristi.caselifecycle.analytics.internal.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.json.JSONObject;
import org.pucar.dristi.caselifecycle.order.OrderApi;
import org.pucar.dristi.common.contract.order.Order;
import org.pucar.dristi.common.contract.order.OrderCriteria;
import org.pucar.dristi.common.contract.order.OrderListResponse;
import org.pucar.dristi.common.contract.order.OrderSearchRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component("analyticsOrderUtil")
public class OrderUtil {

	private final OrderApi orderApi;
	private final ObjectMapper mapper;

	@Autowired
	public OrderUtil(OrderApi orderApi, ObjectMapper mapper) {
		this.orderApi = orderApi;
		this.mapper = mapper;
	}

	public Object getOrder(JSONObject request, String orderNumber, String tenantId) {
		try {
			RequestInfo requestInfo = mapper.convertValue(request.get("RequestInfo"), RequestInfo.class);
			OrderCriteria criteria = new OrderCriteria();
			criteria.setOrderNumber(orderNumber);
			criteria.setTenantId(tenantId);
			OrderSearchRequest searchRequest = new OrderSearchRequest();
			searchRequest.setRequestInfo(requestInfo);
			searchRequest.setCriteria(criteria);
			OrderListResponse response = orderApi.search(searchRequest);
			List<Order> orders = response.getList();
			if (orders == null || orders.isEmpty()) {
				return null;
			}
			JsonNode firstOrder = mapper.valueToTree(orders.get(0));
			return new JSONObject(firstOrder.toString());
		} catch (Exception e) {
			log.error("Error while fetching or processing the order response", e);
			throw new RuntimeException("Error while fetching or processing the order response", e);
		}
	}
}
