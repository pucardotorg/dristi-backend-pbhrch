package org.pucar.dristi.caselifecycle.taskmanagement.internal.service;

import lombok.RequiredArgsConstructor;
import org.pucar.dristi.caselifecycle.taskmanagement.TaskmanagementApi;
import org.pucar.dristi.caselifecycle.taskmanagement.internal.web.models.TaskManagement;
import org.pucar.dristi.caselifecycle.taskmanagement.internal.web.models.TaskManagementRequest;
import org.pucar.dristi.caselifecycle.taskmanagement.internal.web.models.TaskManagementResponse;
import org.pucar.dristi.caselifecycle.taskmanagement.internal.web.models.TaskSearchRequest;
import org.pucar.dristi.common.util.ResponseInfoFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("taskmanagementApiImpl")
@RequiredArgsConstructor
public class TaskmanagementApiImpl implements TaskmanagementApi {

    private final TaskManagementService taskManagementService;
    private final ResponseInfoFactory responseInfoFactory;

    @Override
    public List<TaskManagement> search(TaskSearchRequest request) {
        return taskManagementService.getTaskManagement(request);
    }

    @Override
    public TaskManagementResponse update(TaskManagementRequest request) {
        TaskManagement taskManagement = taskManagementService.updateTaskManagement(request);
        return TaskManagementResponse.builder()
                .taskManagement(taskManagement)
                .responseInfo(responseInfoFactory.createResponseInfoFromRequestInfo(request.getRequestInfo(), true))
                .build();
    }
}
