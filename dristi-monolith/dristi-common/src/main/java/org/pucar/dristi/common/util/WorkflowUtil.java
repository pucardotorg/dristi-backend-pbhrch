// HAND-CURATED — DRISTI workflow toolkit. Originally extracted from
// ab-diary; rewritten after the order migration (Rule 29) to be the
// behavior union of every observed service: setDocuments, setAdditional-
// Details, both return-value flavors. The public surface consistently
// uses DRISTI's `WorkflowObject` / `ProcessInstanceObject` (the egov
// extension that carries `additionalDetails`); egov's plain Workflow /
// ProcessInstance only appears at the boundary with the egov-workflow
// service. Source services with their own `WorkflowService` still own
// domain-specific routing (businessService picking, request→entity
// extraction); this util is the generic layer.
package org.pucar.dristi.common.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.pucar.dristi.common.config.CommonConfiguration;
import org.pucar.dristi.common.models.workflow.ProcessInstanceObject;
import org.pucar.dristi.common.models.workflow.WorkflowObject;
import static org.pucar.dristi.common.config.CommonConstants.*;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.User;
import org.egov.common.contract.workflow.BusinessService;
import org.egov.common.contract.workflow.BusinessServiceResponse;
import org.egov.common.contract.workflow.ProcessInstance;
import org.egov.common.contract.workflow.ProcessInstanceRequest;
import org.egov.common.contract.workflow.ProcessInstanceResponse;
import org.egov.common.contract.workflow.State;
import org.egov.common.contract.models.RequestInfoWrapper;
import org.pucar.dristi.common.repository.ServiceRequestRepository;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service("commonWorkflowUtil")
public class WorkflowUtil {

    @Autowired
    private ServiceRequestRepository repository;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private CommonConfiguration configs;

    /**
     * Searches the BusinessService corresponding to the businessServiceCode.
     */
    public BusinessService getBusinessService(RequestInfo requestInfo, String tenantId, String businessServiceCode) {
        StringBuilder url = getSearchURLWithParams(tenantId, businessServiceCode);
        RequestInfoWrapper requestInfoWrapper = RequestInfoWrapper.builder().requestInfo(requestInfo).build();
        Object result = repository.fetchResult(url, requestInfoWrapper);
        BusinessServiceResponse response;
        try {
            response = mapper.convertValue(result, BusinessServiceResponse.class);
        } catch (IllegalArgumentException e) {
            throw new CustomException(PARSING_ERROR, FAILED_TO_PARSE_BUSINESS_SERVICE_SEARCH);
        }
        if (CollectionUtils.isEmpty(response.getBusinessServices())) {
            throw new CustomException(BUSINESS_SERVICE_NOT_FOUND, THE_BUSINESS_SERVICE + businessServiceCode + NOT_FOUND);
        }
        return response.getBusinessServices().get(0);
    }

    /**
     * Calls the workflow service and returns the resulting state's
     * {@code state} name (e.g. "PUBLISHED"). Use this when callers compare
     * against state-machine state names — the common case.
     */
    public String updateWorkflowStatus(RequestInfo requestInfo, String tenantId, String businessId,
                                       String businessServiceCode, WorkflowObject workflow, String wfModuleName) {
        return doUpdate(requestInfo, tenantId, businessId, businessServiceCode, workflow, wfModuleName).getState();
    }

    /**
     * Calls the workflow service and returns the resulting state's
     * {@code applicationStatus} (egov-defined application lifecycle).
     * Use this only when the workflow YAML's applicationStatus carries
     * the value the caller compares against.
     */
    public String updateWorkflowApplicationStatus(RequestInfo requestInfo, String tenantId, String businessId,
                                                  String businessServiceCode, WorkflowObject workflow, String wfModuleName) {
        return doUpdate(requestInfo, tenantId, businessId, businessServiceCode, workflow, wfModuleName).getApplicationStatus();
    }

    private State doUpdate(RequestInfo requestInfo, String tenantId, String businessId,
                           String businessServiceCode, WorkflowObject workflow, String wfModuleName) {
        ProcessInstanceObject processInstance = getProcessInstanceForWorkflow(
                requestInfo, tenantId, businessId, businessServiceCode, workflow, wfModuleName);
        ProcessInstanceRequest workflowRequest = new ProcessInstanceRequest(requestInfo, Collections.singletonList(processInstance));
        return callWorkFlow(workflowRequest);
    }

    private StringBuilder getSearchURLWithParams(String tenantId, String businessService) {
        StringBuilder url = new StringBuilder(configs.getWfHost());
        url.append(configs.getWfBusinessServiceSearchPath());
        url.append(TENANTID);
        url.append(tenantId);
        url.append(BUSINESS_SERVICES);
        url.append(businessService);
        return url;
    }

    /**
     * Builds a {@link ProcessInstanceObject} for a workflow transition.
     * Sets {@code documents} and {@code additionalDetails} so the workflow
     * service receives the fields every DRISTI service relies on.
     */
    public ProcessInstanceObject getProcessInstanceForWorkflow(RequestInfo requestInfo, String tenantId,
                                                               String businessId, String businessServiceCode,
                                                               WorkflowObject workflow, String wfModuleName) {
        ProcessInstanceObject processInstance = new ProcessInstanceObject();
        processInstance.setBusinessId(businessId);
        processInstance.setAction(workflow.getAction());
        processInstance.setModuleName(wfModuleName);
        processInstance.setTenantId(tenantId);
        processInstance.setBusinessService(getBusinessService(requestInfo, tenantId, businessServiceCode).getBusinessService());
        processInstance.setDocuments(workflow.getDocuments());
        processInstance.setComment(workflow.getComments());
        processInstance.setAdditionalDetails(workflow.getAdditionalDetails());
        if (!CollectionUtils.isEmpty(workflow.getAssignes())) {
            processInstance.setAssignes(getUserListFromUserUuid(workflow.getAssignes()));
        }
        return processInstance;
    }

    /**
     * Inverse of {@link #getProcessInstanceForWorkflow} — builds DRISTI
     * {@link WorkflowObject}s from the result of a workflow search.
     * Preserves {@code additionalDetails} when the inputs are
     * {@link ProcessInstanceObject} (the egov response only deserialises
     * to base {@link ProcessInstance}, so the cast is conservative).
     */
    public Map<String, WorkflowObject> getWorkflow(List<ProcessInstance> processInstances) {
        Map<String, WorkflowObject> businessIdToWorkflow = new HashMap<>();
        processInstances.forEach(processInstance -> {
            WorkflowObject workflow = getWorkflowFromProcessInstance(processInstance);
            if (workflow != null) {
                List<String> userIds = null;
                if (!CollectionUtils.isEmpty(processInstance.getAssignes())) {
                    userIds = processInstance.getAssignes().stream().map(User::getUuid).collect(Collectors.toList());
                }
                workflow.setAssignes(userIds);
                workflow.setDocuments(processInstance.getDocuments());
                businessIdToWorkflow.put(processInstance.getBusinessId(), workflow);
            }
        });
        return businessIdToWorkflow;
    }

    /**
     * Single-process-instance variant of {@link #getWorkflow}. Returns
     * {@link WorkflowObject} (DRISTI extension), preserving
     * {@code additionalDetails} when the input is actually a
     * {@link ProcessInstanceObject}.
     */
    public WorkflowObject getWorkflowFromProcessInstance(ProcessInstance processInstance) {
        if (processInstance == null) {
            return null;
        }
        State state = processInstance.getState();
        WorkflowObject workflow = new WorkflowObject();
        workflow.setAction(state != null ? state.getState() : null);
        workflow.setComments(processInstance.getComment());
        if (processInstance instanceof ProcessInstanceObject po) {
            workflow.setAdditionalDetails(po.getAdditionalDetails());
        }
        return workflow;
    }

    /**
     * Materialises a UUID list into stub {@link User} objects with the UUID
     * set — the shape the workflow service expects on
     * {@code processInstance.assignes}. Promoted from per-service helpers.
     */
    public List<User> getUserListFromUserUuid(List<String> uuids) {
        List<User> users = new ArrayList<>();
        if (CollectionUtils.isEmpty(uuids)) {
            return users;
        }
        uuids.forEach(uuid -> {
            User user = new User();
            user.setUuid(uuid);
            users.add(user);
        });
        return users;
    }

    /**
     * Issues the workflow transition POST and returns the first process
     * instance's resulting {@link State}. Public so service-local
     * {@code WorkflowService} classes can reuse without re-implementing.
     */
    public State callWorkFlow(ProcessInstanceRequest workflowReq) {
        StringBuilder url = new StringBuilder(configs.getWfHost().concat(configs.getWfTransitionPath()));
        Object optional = repository.fetchResult(url, workflowReq);
        ProcessInstanceResponse response = mapper.convertValue(optional, ProcessInstanceResponse.class);
        return response.getProcessInstances().get(0).getState();
    }
}
