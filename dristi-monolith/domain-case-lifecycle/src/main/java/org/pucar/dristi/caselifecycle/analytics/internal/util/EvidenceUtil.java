package org.pucar.dristi.caselifecycle.analytics.internal.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.json.JSONObject;
import org.pucar.dristi.caselifecycle.evidence.EvidenceApi;
import org.pucar.dristi.common.contract.evidence.Artifact;
import org.pucar.dristi.common.contract.evidence.EvidenceSearchCriteria;
import org.pucar.dristi.common.contract.evidence.EvidenceSearchResponse;
import org.pucar.dristi.common.contract.evidence.Pagination;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component("analyticsEvidenceUtil")
public class EvidenceUtil {

	private final EvidenceApi evidenceApi;
	private final ObjectMapper mapper;

	@Autowired
	public EvidenceUtil(EvidenceApi evidenceApi, ObjectMapper mapper) {
		this.evidenceApi = evidenceApi;
		this.mapper = mapper;
	}

	public Object getEvidence(JSONObject request, String tenantId, String artifactNumber) {
		try {
			RequestInfo requestInfo = mapper.convertValue(request.get("RequestInfo"), RequestInfo.class);
			EvidenceSearchCriteria criteria = EvidenceSearchCriteria.builder()
					.artifactNumber(artifactNumber)
					.tenantId(tenantId)
					.build();
			EvidenceSearchResponse response = evidenceApi.searchEvidence(requestInfo, criteria, defaultPagination());
			List<Artifact> artifacts = response.getArtifacts();
			if (artifacts == null || artifacts.isEmpty()) {
				return null;
			}
			JsonNode firstArtifact = mapper.valueToTree(artifacts.get(0));
			return new JSONObject(firstArtifact.toString());
		} catch (Exception e) {
			log.error("Error while fetching or processing the evidence response", e);
			throw new RuntimeException("Error while fetching or processing the evidence response", e);
		}
	}

	private static Pagination defaultPagination() {
		return Pagination.builder().limit(100.0).offSet(0.0).build();
	}
}
