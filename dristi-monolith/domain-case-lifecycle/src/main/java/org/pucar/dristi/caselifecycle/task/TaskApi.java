package org.pucar.dristi.caselifecycle.task;

import org.pucar.dristi.common.contract.task.TaskRequest;
import org.pucar.dristi.common.contract.task.TaskResponse;

/**
 * Public, cross-subdomain API of the task subdomain. Other modules
 * (cases today) consume task through this interface — never by
 * importing from {@code internal/}.
 *
 * <p>Contract DTOs live in {@code dristi-common/contract/task/} per
 * Rule 24 (Phase 35 lift).
 */
public interface TaskApi {

    /**
     * Create a task. The request carries {@link org.egov.common.contract.request.RequestInfo}
     * (caller identity) and the {@link org.pucar.dristi.common.contract.task.Task} payload.
     * Behaviour matches the {@code /task/v1/create} REST contract: validates, enriches,
     * publishes to kafka, runs the workflow transition, and (for SUMMONS/WARRANT/
     * PROCLAMATION/ATTACHMENT task types) updates the linked case.
     */
    TaskResponse createTask(TaskRequest request);
}
