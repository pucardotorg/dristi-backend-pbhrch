package org.pucar.dristi.caselifecycle.task.internal.service;

import lombok.RequiredArgsConstructor;
import org.pucar.dristi.caselifecycle.task.TaskApi;
import org.pucar.dristi.common.contract.task.Task;
import org.pucar.dristi.common.contract.task.TaskCase;
import org.pucar.dristi.common.contract.task.TaskCaseSearchRequest;
import org.pucar.dristi.common.contract.task.TaskRequest;
import org.pucar.dristi.common.contract.task.TaskSearchRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("taskApiImpl")
@RequiredArgsConstructor
public class TaskApiImpl implements TaskApi {

    private final TaskService taskService;

    @Override
    public List<Task> search(TaskSearchRequest request) {
        return taskService.searchTask(request);
    }

    @Override
    public Task create(TaskRequest request) {
        return taskService.createTask(request);
    }

    @Override
    public Task update(TaskRequest request) {
        return taskService.updateTask(request);
    }

    @Override
    public List<TaskCase> searchTable(TaskCaseSearchRequest request) {
        return taskService.searchCaseTask(request);
    }
}
