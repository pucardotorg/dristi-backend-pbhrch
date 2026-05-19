package org.pucar.dristi.caselifecycle.task.internal.service;

import org.pucar.dristi.caselifecycle.task.TaskApi;
import org.pucar.dristi.common.contract.task.Task;
import org.pucar.dristi.common.contract.task.TaskCase;
import org.pucar.dristi.common.contract.task.TaskCaseSearchRequest;
import org.pucar.dristi.common.contract.task.TaskSearchRequest;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TaskApiImpl implements TaskApi {

    private final TaskService taskService;

    public TaskApiImpl(TaskService taskService) {
        this.taskService = taskService;
    }

    @Override
    public List<Task> search(TaskSearchRequest request) {
        return taskService.searchTask(request);
    }

    @Override
    public List<TaskCase> searchTable(TaskCaseSearchRequest request) {
        return taskService.searchCaseTask(request);
    }
}
