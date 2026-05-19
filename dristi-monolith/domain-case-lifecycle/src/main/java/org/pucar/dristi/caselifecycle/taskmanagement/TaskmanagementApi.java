package org.pucar.dristi.caselifecycle.taskmanagement;

import org.pucar.dristi.caselifecycle.taskmanagement.internal.web.models.TaskManagement;
import org.pucar.dristi.caselifecycle.taskmanagement.internal.web.models.TaskManagementRequest;
import org.pucar.dristi.caselifecycle.taskmanagement.internal.web.models.TaskManagementResponse;
import org.pucar.dristi.caselifecycle.taskmanagement.internal.web.models.TaskSearchRequest;

import java.util.List;

/**
 * Public API of the taskmanagement subdomain. Other subdomains (hearing today)
 * consume taskmanagement through this interface — never by importing from
 * internal/.
 */
public interface TaskmanagementApi {

    List<TaskManagement> search(TaskSearchRequest request);

    TaskManagementResponse update(TaskManagementRequest request);
}
