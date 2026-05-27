package org.pucar.dristi.caselifecycle.analytics.internal.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.json.JSONObject;
import org.pucar.dristi.common.hearing.HearingApi;
import org.pucar.dristi.common.contract.hearing.Hearing;
import org.pucar.dristi.common.contract.hearing.HearingCriteria;
import org.pucar.dristi.common.contract.hearing.HearingSearchRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component("analyticsHearingUtil")
public class HearingUtil {

	private final HearingApi hearingApi;
	private final ObjectMapper mapper;

	@Autowired
	public HearingUtil(HearingApi hearingApi, ObjectMapper mapper) {
		this.hearingApi = hearingApi;
		this.mapper = mapper;
	}

	public Object getHearing(JSONObject request, String applicationNumber, String cnrNumber, String hearingId, String tenantId) {
		try {
			RequestInfo requestInfo = mapper.convertValue(request.get("RequestInfo"), RequestInfo.class);
			HearingCriteria criteria = HearingCriteria.builder()
					.applicationNumber(applicationNumber)
					.cnrNumber(cnrNumber)
					.hearingId(hearingId)
					.tenantId(tenantId)
					.build();
			HearingSearchRequest searchRequest = HearingSearchRequest.builder()
					.requestInfo(requestInfo)
					.criteria(criteria)
					.build();
			List<Hearing> hearings = hearingApi.search(searchRequest);
			if (hearings == null || hearings.isEmpty()) {
				return null;
			}
			JsonNode firstHearing = mapper.valueToTree(hearings.get(0));
			return new JSONObject(firstHearing.toString());
		} catch (Exception e) {
			log.error("Error while fetching or processing the hearing response", e);
			throw new RuntimeException("Error while fetching or processing the hearing response", e);
		}
	}
}
