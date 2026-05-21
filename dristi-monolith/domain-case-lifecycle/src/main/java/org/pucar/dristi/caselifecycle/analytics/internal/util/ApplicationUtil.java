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

import static org.pucar.dristi.caselifecycle.analytics.internal.config.ServiceConstants.APPLICATION_PATH;

@Slf4j
@Component("analyticsApplicationUtil")
public class ApplicationUtil {

	private final Configuration config;
	private final ServiceRequestRepository repository;
	private final Util util;
	private final ObjectMapper mapper;

	@Autowired
	public ApplicationUtil(Configuration config, ServiceRequestRepository repository, Util util, ObjectMapper mapper) {
		this.config = config;
		this.repository = repository;
		this.util = util;
		this.mapper = mapper;
	}

	public Object getApplication(JSONObject request, String tenantId, String applicationNumber) {
		StringBuilder url = getSearchURLWithParams();
		log.info("Inside ApplicationUtil getApplication :: URL: {}", url);

		request.put("tenantId", tenantId);
		JSONObject criteria = new JSONObject();
		criteria.put("applicationNumber", applicationNumber);
		criteria.put("tenantId", tenantId);
		request.put("criteria", criteria);

		log.info("Inside ApplicationUtil getApplication :: Request: {}", request);

		try {
			Object responseObj = repository.fetchResult(url, request);
			String response = responseObj == null ? null : mapper.writeValueAsString(responseObj);
			log.info("Inside ApplicationUtil getApplication :: Response: {}", response);
			JSONArray applications = util.constructArray(response, APPLICATION_PATH);
			return applications.length() > 0 ? applications.get(0) : null;
		} catch (Exception e) {
			log.error("Error while processing application response", e);
			throw new RuntimeException("Error while processing application response", e);
		}
	}

	private StringBuilder getSearchURLWithParams() {
		return new StringBuilder(config.getApplicationHost())
				.append(config.getApplicationSearchPath());
	}
}
