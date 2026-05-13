package org.pucar.dristi.caselifecycle.task.internal.service;

import org.egov.common.contract.response.ResponseInfo;
import org.pucar.dristi.caselifecycle.task.TaskApi;
import org.pucar.dristi.common.contract.task.Task;
import org.pucar.dristi.common.contract.task.TaskRequest;
import org.pucar.dristi.common.contract.task.TaskResponse;
import org.pucar.dristi.common.util.ResponseInfoFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TaskApiImpl implements TaskApi {

    private final TaskService taskService;
    private final ResponseInfoFactory responseInfoFactory;

    @Autowired
    public TaskApiImpl(TaskService taskService, ResponseInfoFactory responseInfoFactory) {
        this.taskService = taskService;
        this.responseInfoFactory = responseInfoFactory;
    }

    @Override
    public TaskResponse createTask(TaskRequest request) {
        Task task = taskService.createTask(request);
        ResponseInfo responseInfo = responseInfoFactory.createResponseInfoFromRequestInfo(
                request.getRequestInfo(), true);
        return TaskResponse.builder().task(task).responseInfo(responseInfo).build();
    }
}
