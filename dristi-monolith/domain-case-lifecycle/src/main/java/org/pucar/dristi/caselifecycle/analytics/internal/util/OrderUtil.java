package org.pucar.dristi.caselifecycle.analytics.internal.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.pucar.dristi.caselifecycle.analytics.internal.config.Configuration;
import org.pucar.dristi.common.repository.ServiceRequestRepository;
import org.pucar.dristi.caselifecycle.analytics.internal.util.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.pucar.dristi.caselifecycle.analytics.internal.config.ServiceConstants.*;

@Slf4j
@Component("analyticsOrderUtil")
public class OrderUtil {

	private final Configuration config;
	private final ServiceRequestRepository repository;
	private final Util util;
	private final ObjectMapper mapper;

	@Autowired
	public OrderUtil(Configuration config, ServiceRequestRepository repository, Util util, ObjectMapper mapper) {
		this.config = config;
		this.repository = repository;
		this.util = util;
		this.mapper = mapper;
	}

	public Object getOrder(JSONObject request, String orderNumber, String tenantId) {
		StringBuilder url = getSearchURLWithParams();
		log.info("Inside OrderUtil getOrder :: URL: {}", url);
		JSONObject criteria = new JSONObject();
		criteria.put("orderNumber", orderNumber);
		criteria.put("tenantId", tenantId);
		request.put("criteria", criteria);
		request.put("tenantId", tenantId);

		log.info("Inside Order util getOrder :: Request: {}", request);

		try {
			Object responseObj = repository.fetchResult(url, request);
			String response = responseObj == null ? null : mapper.writeValueAsString(responseObj);
			log.info("Inside OrderUtil getOrder :: Response: {}", response);

			JSONArray orders = util.constructArray(response, ORDER_PATH);
			return orders.length() > 0 ? orders.get(0) : null;
		} catch (Exception e) {
			log.error("Error while fetching or processing the order response", e);
			throw new RuntimeException("Error while fetching or processing the order response", e);
		}
	}

	private StringBuilder getSearchURLWithParams() {
		StringBuilder url = new StringBuilder(config.getOrderHost());
		url.append(config.getOrderSearchPath());
		return url;
	}
}
