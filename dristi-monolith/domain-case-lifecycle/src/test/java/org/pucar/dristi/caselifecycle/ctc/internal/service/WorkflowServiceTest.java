package org.pucar.dristi.caselifecycle.ctc.internal.service;

import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.User;
import org.egov.common.contract.workflow.ProcessInstanceRequest;
import org.egov.common.contract.workflow.State;
import org.egov.tracer.model.CustomException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pucar.dristi.caselifecycle.ctc.internal.config.Configuration;
import org.pucar.dristi.caselifecycle.ctc.internal.web.models.*;
import org.pucar.dristi.common.contract.ctc.*;
import org.pucar.dristi.common.util.WorkflowUtil;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.pucar.dristi.common.models.workflow.WorkflowObject;
import org.pucar.dristi.common.models.workflow.ProcessInstanceObject;

@ExtendWith(MockitoExtension.class)
class WorkflowServiceTest {

    @Mock private WorkflowUtil workflowUtil;
    @Mock private Configuration config;

    @InjectMocks
    private WorkflowService workflowService;

    private CtcApplication application;
    private RequestInfo requestInfo;

    @BeforeEach
    void setUp() {
        requestInfo = RequestInfo.builder()
                .userInfo(User.builder().uuid("user-1").build())
                .build();

        WorkflowObject workflow = new WorkflowObject();
        workflow.setAction("APPROVE");

        application = CtcApplication.builder()
                .ctcApplicationNumber("CA-001")
                .tenantId("pb")
                .workflow(workflow)
                .build();

        lenient().when(config.getCtcBusinessName()).thenReturn("ctc");
        lenient().when(config.getCtcBusinessServiceName()).thenReturn("ctc-services");
    }

    @Test
    void updateWorkflowStatus_shouldSetStatusFromWorkflowResponse() {
        State state = new State();
        state.setState("APPROVED");
        when(workflowUtil.callWorkFlow(any())).thenReturn(state);

        workflowService.updateWorkflowStatus(application, requestInfo);

        assertEquals("APPROVED", application.getStatus());
    }

    @Test
    void updateWorkflowStatus_shouldThrowCustomExceptionOnError() {
        when(workflowUtil.callWorkFlow(any())).thenThrow(new RuntimeException("WF error"));

        assertThrows(CustomException.class, () -> workflowService.updateWorkflowStatus(application, requestInfo));
    }

    @Test
    void updateWorkflowStatus_shouldRethrowCustomException() {
        when(workflowUtil.callWorkFlow(any()))
                .thenThrow(new CustomException("WF_ERROR", "workflow error"));

        CustomException ex = assertThrows(CustomException.class,
                () -> workflowService.updateWorkflowStatus(application, requestInfo));
        assertEquals("WF_ERROR", ex.getCode());
    }

    @Test
    void callWorkFlow_shouldDelegateToCanonical() {
        State expected = new State();
        expected.setState("PENDING_PAYMENT");
        ProcessInstanceRequest req = new ProcessInstanceRequest();
        when(workflowUtil.callWorkFlow(req)).thenReturn(expected);

        State result = workflowService.callWorkFlow(req);

        assertEquals("PENDING_PAYMENT", result.getState());
        verify(workflowUtil).callWorkFlow(req);
    }

    @Test
    void getProcessInstance_shouldMapFieldsCorrectly() {
        WorkflowObject workflow = new WorkflowObject();
        workflow.setAction("SUBMIT");
        workflow.setComments("Test comment");
        workflow.setAssignes(List.of("uuid-1", "uuid-2"));
        application.setWorkflow(workflow);

        ProcessInstanceObject result = workflowService.getProcessInstance(application);

        assertEquals("CA-001", result.getBusinessId());
        assertEquals("SUBMIT", result.getAction());
        assertEquals("ctc", result.getModuleName());
        assertEquals("pb", result.getTenantId());
        assertEquals("ctc-services", result.getBusinessService());
        assertEquals("Test comment", result.getComment());
        assertEquals(2, result.getAssignes().size());
    }

    @Test
    void getProcessInstance_shouldHandleNullAssignes() {
        WorkflowObject workflow = new WorkflowObject();
        workflow.setAction("SUBMIT");
        workflow.setAssignes(null);
        application.setWorkflow(workflow);

        ProcessInstanceObject result = workflowService.getProcessInstance(application);

        assertNull(result.getAssignes());
    }

    @Test
    void getProcessInstance_shouldHandleEmptyAssignes() {
        WorkflowObject workflow = new WorkflowObject();
        workflow.setAction("SUBMIT");
        workflow.setAssignes(Collections.emptyList());
        application.setWorkflow(workflow);

        ProcessInstanceObject result = workflowService.getProcessInstance(application);

        assertNull(result.getAssignes());
    }
}
