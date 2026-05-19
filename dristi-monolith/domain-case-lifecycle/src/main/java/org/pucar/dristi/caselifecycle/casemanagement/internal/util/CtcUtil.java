package org.pucar.dristi.caselifecycle.casemanagement.internal.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.pucar.dristi.caselifecycle.casemanagement.internal.config.Configuration;
import org.pucar.dristi.caselifecycle.ctc.CtcApi;
import org.pucar.dristi.common.contract.ctc.CtcApplication;
import org.pucar.dristi.common.contract.ctc.CtcApplicationSearchCriteria;
import org.pucar.dristi.common.contract.ctc.CtcApplicationSearchRequest;
import org.pucar.dristi.common.repository.ServiceRequestRepository;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component
@Slf4j
public class CtcUtil {

    private final ObjectMapper mapper;
    private final Configuration configs;
    private final ServiceRequestRepository repository;
    private final CtcApi ctcApi;

    public CtcUtil(ObjectMapper mapper, Configuration configs, ServiceRequestRepository repository, CtcApi ctcApi) {
        this.mapper = mapper;
        this.configs = configs;
        this.repository = repository;
        this.ctcApi = ctcApi;
    }

    public Boolean isPartyToCase(String ctcApplicationNumber, String courtId, RequestInfo requestInfo) {
        if (!StringUtils.hasText(ctcApplicationNumber) || !StringUtils.hasText(courtId)) {
            return null;
        }

        String tenantId = deriveTenantIdFromCourtId(courtId);
        if (!StringUtils.hasText(tenantId)) {
            return null;
        }

        try {
            CtcApplicationSearchRequest request = CtcApplicationSearchRequest.builder()
                    .requestInfo(requestInfo != null ? requestInfo : RequestInfo.builder().build())
                    .criteria(CtcApplicationSearchCriteria.builder()
                            .tenantId(tenantId)
                            .ctcApplicationNumber(ctcApplicationNumber)
                            .build())
                    .build();
            List<CtcApplication> applications = ctcApi.search(request);
            if (applications == null || applications.isEmpty()) {
                return null;
            }
            return applications.get(0).getIsPartyToCase();
        } catch (Exception e) {
            log.error("Error searching CTC application for ctcApplicationNumber: {}", ctcApplicationNumber, e);
            return null;
        }
    }

    public void updateCtcApplication(Map<String, Object> ctcApplication, RequestInfo requestInfo) {
        // Cross-subdomain write — kept on REST per Rule 35 (write-side
        // *Api exposure deferred until a deliberate design pass on
        // cross-subdomain mutators). See ctc/package-info.java.
        StringBuilder uri = new StringBuilder();
        uri.append(configs.getCtcHost()).append(configs.getCtcUpdateEndpoint());

        Map<String, Object> request = new HashMap<>();
        request.put("RequestInfo", requestInfo != null ? requestInfo : RequestInfo.builder().build());
        request.put("ctcApplication", ctcApplication);

        try {
            repository.fetchResult(uri, request);
            log.info("Successfully updated CTC application");
        } catch (Exception e) {
            log.error("Error updating CTC application", e);
        }
    }

    private String deriveTenantIdFromCourtId(String courtId) {
        if (!StringUtils.hasText(courtId) || courtId.length() < 2) {
            return null;
        }
        return courtId.substring(0, 2).toLowerCase(Locale.ROOT);
    }
}
