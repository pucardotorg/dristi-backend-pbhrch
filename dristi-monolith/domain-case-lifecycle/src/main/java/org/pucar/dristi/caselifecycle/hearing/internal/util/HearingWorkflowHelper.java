package org.pucar.dristi.caselifecycle.hearing.internal.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.models.RequestInfoWrapper;
import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.pucar.dristi.caselifecycle.hearing.internal.config.Configuration;
import org.pucar.dristi.caselifecycle.hearing.internal.web.models.ProcessInstance;
import org.pucar.dristi.caselifecycle.hearing.internal.web.models.ProcessInstanceResponse;
import org.pucar.dristi.common.repository.ServiceRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;

import static org.pucar.dristi.caselifecycle.hearing.internal.config.ServiceConstants.WORKFLOW_SERVICE_EXCEPTION;

@Component
@Slf4j
public class HearingWorkflowHelper {

    private final ServiceRequestRepository repository;
    private final ObjectMapper mapper;
    private final Configuration configs;

    @Autowired
    public HearingWorkflowHelper(ServiceRequestRepository repository, ObjectMapper mapper, Configuration configs) {
        this.repository = repository;
        this.mapper = mapper;
        this.configs = configs;
    }

    public List<ProcessInstance> getProcessInstance(RequestInfo requestInfo, String tenantId, String businessId) {
        try {
            RequestInfoWrapper requestInfoWrapper = RequestInfoWrapper.builder().requestInfo(requestInfo).build();
            StringBuilder url = getSearchURLForProcessInstanceWithParams(tenantId, businessId);
            Object res = repository.fetchResult(url, requestInfoWrapper);
            ProcessInstanceResponse response = mapper.convertValue(res, ProcessInstanceResponse.class);
            if (response != null && !CollectionUtils.isEmpty(response.getProcessInstances()) && response.getProcessInstances().get(0) != null)
                return response.getProcessInstances();
            return Collections.emptyList();
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error getting current workflow: {}", e.toString());
            throw new CustomException(WORKFLOW_SERVICE_EXCEPTION, e.toString());
        }
    }

    StringBuilder getSearchURLForProcessInstanceWithParams(String tenantId, String businessService) {
        StringBuilder url = new StringBuilder(configs.getWfHost());
        url.append(configs.getWfProcessInstanceSearchPath());
        url.append("?tenantId=").append(tenantId);
        url.append("&businessIds=").append(businessService);
        url.append("&history=true");
        return url;
    }
}
