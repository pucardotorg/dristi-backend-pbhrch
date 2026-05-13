package org.pucar.dristi.caselifecycle.ctc.internal.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.User;
import org.egov.common.contract.workflow.ProcessInstanceRequest;
import org.egov.common.contract.workflow.State;
import org.egov.tracer.model.CustomException;
import org.pucar.dristi.caselifecycle.ctc.internal.config.Configuration;
import org.pucar.dristi.caselifecycle.ctc.internal.web.models.*;
import org.pucar.dristi.common.contract.ctc.*;
import org.pucar.dristi.common.util.WorkflowUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import lombok.extern.slf4j.Slf4j;

import org.pucar.dristi.common.models.workflow.WorkflowObject;
import org.pucar.dristi.common.models.workflow.ProcessInstanceObject;

@Component("ctcWorkflowService")
@Slf4j
public class WorkflowService {

    private final WorkflowUtil workflowUtil;

    private final Configuration config;

    @Autowired
    public WorkflowService(WorkflowUtil workflowUtil, Configuration config) {
        this.workflowUtil = workflowUtil;
        this.config = config;
    }

    public void updateWorkflowStatus(CtcApplication ctcApplication, RequestInfo requestInfo) {
        try {
            ProcessInstanceObject processInstance = getProcessInstance(ctcApplication);
            ProcessInstanceRequest workflowRequest = new ProcessInstanceRequest(requestInfo, Collections.singletonList(processInstance));
            log.info("ProcessInstance Request :: {}", workflowRequest);
            String state = callWorkFlow(workflowRequest).getState();
            log.info("Workflow State for ctc application number :: {} and state :: {}", ctcApplication.getCtcApplicationNumber(), state);
            ctcApplication.setStatus(state);
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error updating workflow status :: {}", e.toString());
            throw new CustomException("WORKFLOW_SERVICE_EXCEPTION", "Error updating workflow status: " + e.getMessage());
        }
    }

    public State callWorkFlow(ProcessInstanceRequest workflowReq) {
        return workflowUtil.callWorkFlow(workflowReq);
    }

    public ProcessInstanceObject getProcessInstance(CtcApplication ctcApplication) {
        try {
            WorkflowObject workflow = ctcApplication.getWorkflow();
            ProcessInstanceObject processInstance = new ProcessInstanceObject();
            processInstance.setBusinessId(ctcApplication.getCtcApplicationNumber());
            processInstance.setAction(workflow.getAction());
            processInstance.setModuleName(config.getCtcBusinessName());
            processInstance.setTenantId(ctcApplication.getTenantId());
            processInstance.setBusinessService(config.getCtcBusinessServiceName());
            processInstance.setDocuments(workflow.getDocuments());
            processInstance.setComment(workflow.getComments());
            processInstance.setAdditionalDetails(workflow.getAdditionalDetails());
            if (!CollectionUtils.isEmpty(workflow.getAssignes())) {
                List<User> users = new ArrayList<>();
                workflow.getAssignes().forEach(uuid -> {
                    User user = new User();
                    user.setUuid(uuid);
                    users.add(user);
                });
                processInstance.setAssignes(users);
            }
            return processInstance;
        } catch (Exception e) {
            log.error("Error getting process instance for CASE :: {}", e.toString());
            throw new CustomException("WORKFLOW_SERVICE_EXCEPTION", e.getMessage());
        }
    }
}
