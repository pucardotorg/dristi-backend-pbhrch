package org.pucar.dristi.caselifecycle.casemanagement.internal.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.pucar.dristi.caselifecycle.casemanagement.internal.web.models.task.TaskCase;
import org.pucar.dristi.caselifecycle.casemanagement.internal.web.models.task.TaskCaseSearchCriteria;
import org.pucar.dristi.caselifecycle.task.TaskApi;
import org.pucar.dristi.common.contract.casemanagement.Task;
import org.pucar.dristi.common.contract.casemanagement.TaskCriteria;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

import static org.pucar.dristi.caselifecycle.casemanagement.internal.config.ServiceConstants.SEARCHER_SERVICE_EXCEPTION;

@Component("casemanagementTaskUtil")
@Slf4j
public class TaskUtil {

    private final TaskApi taskApi;
    private final ObjectMapper objectMapper;

    public TaskUtil(TaskApi taskApi, ObjectMapper objectMapper) {
        this.taskApi = taskApi;
        this.objectMapper = objectMapper;
    }

    public List<Task> searchTask(TaskCriteria criteria, RequestInfo requestInfo) {
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        try {
            org.pucar.dristi.common.contract.task.TaskCriteria taskCriteria =
                    objectMapper.convertValue(criteria, org.pucar.dristi.common.contract.task.TaskCriteria.class);
            org.pucar.dristi.common.contract.task.TaskSearchRequest request =
                    new org.pucar.dristi.common.contract.task.TaskSearchRequest();
            request.setRequestInfo(requestInfo);
            request.setCriteria(taskCriteria);
            List<org.pucar.dristi.common.contract.task.Task> results = taskApi.search(request);
            if (results == null || results.isEmpty()) {
                return Collections.emptyList();
            }
            return objectMapper.convertValue(
                    results,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Task.class));
        } catch (Exception e) {
            log.error(SEARCHER_SERVICE_EXCEPTION, e);
            throw new CustomException();
        }
    }

    public List<TaskCase> searchTaskTable(TaskCaseSearchCriteria criteria, RequestInfo requestInfo) {
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        try {
            org.pucar.dristi.common.contract.task.TaskCaseSearchCriteria taskCriteria =
                    objectMapper.convertValue(criteria, org.pucar.dristi.common.contract.task.TaskCaseSearchCriteria.class);
            org.pucar.dristi.common.contract.task.TaskCaseSearchRequest request =
                    new org.pucar.dristi.common.contract.task.TaskCaseSearchRequest();
            request.setRequestInfo(requestInfo);
            request.setCriteria(taskCriteria);
            org.pucar.dristi.common.contract.task.Pagination pagination =
                    new org.pucar.dristi.common.contract.task.Pagination();
            pagination.setSortBy("createdDate");
            pagination.setOrder(org.pucar.dristi.common.contract.task.Order.ASC);
            pagination.setLimit(100.0);
            request.setPagination(pagination);
            List<org.pucar.dristi.common.contract.task.TaskCase> results = taskApi.searchTable(request);
            if (results == null || results.isEmpty()) {
                return Collections.emptyList();
            }
            return objectMapper.convertValue(
                    results,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, TaskCase.class));
        } catch (Exception e) {
            log.error(SEARCHER_SERVICE_EXCEPTION, e);
            throw new CustomException(SEARCHER_SERVICE_EXCEPTION, e.getMessage());
        }
    }
}
