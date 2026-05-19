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

import static org.pucar.dristi.caselifecycle.analytics.internal.config.ServiceConstants.ARTIFACT_PATH;

@Slf4j
@Component("analyticsEvidenceUtil")
public class EvidenceUtil {

	private final Configuration config;
	private final ServiceRequestRepository repository;
	private final Util util;
	private final ObjectMapper mapper;

	@Autowired
	public EvidenceUtil(Configuration config, ServiceRequestRepository repository, Util util, ObjectMapper mapper) {
		this.config = config;
		this.repository = repository;
		this.util = util;
		this.mapper = mapper;
	}

	public Object getEvidence(JSONObject request, String tenantId, String artifactNumber) {
		StringBuilder url = getSearchURLWithParams();
		log.info("Inside EvidenceUtil getEvidence :: URL: {}", url);

		request.put("tenantId", tenantId);

		JSONObject criteria = new JSONObject();
		if (artifactNumber != null) {
			criteria.put("artifactNumber", artifactNumber);
			criteria.put("tenantId", tenantId);
		}
		request.put("criteria", criteria);

		try {
			Object responseObj = repository.fetchResult(url, request);
			String response = responseObj == null ? null : mapper.writeValueAsString(responseObj);
			log.info("Inside EvidenceUtil getEvidence :: Response: {}", response);

			JSONArray artifacts = util.constructArray(response, ARTIFACT_PATH);
			return artifacts.length() > 0 ? artifacts.get(0) : null;
		} catch (Exception e) {
			log.error("Error while fetching or processing the evidence response", e);
			throw new RuntimeException("Error while fetching or processing the evidence response", e);
		}
	}

	private StringBuilder getSearchURLWithParams() {
		return new StringBuilder(config.getEvidenceHost())
				.append(config.getEvidenceSearchPath());
	}
}
