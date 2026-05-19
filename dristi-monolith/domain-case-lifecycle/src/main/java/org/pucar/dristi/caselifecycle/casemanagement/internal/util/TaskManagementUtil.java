package org.pucar.dristi.caselifecycle.casemanagement.internal.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.slf4j.Slf4j;
import org.egov.tracer.model.CustomException;
import org.pucar.dristi.caselifecycle.casemanagement.internal.web.models.taskManagement.TaskManagement;
import org.pucar.dristi.caselifecycle.casemanagement.internal.web.models.taskManagement.TaskSearchRequest;
import org.pucar.dristi.caselifecycle.taskmanagement.TaskmanagementApi;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

import static org.pucar.dristi.caselifecycle.casemanagement.internal.config.ServiceConstants.SEARCHER_SERVICE_EXCEPTION;

@Component("casemanagementTaskManagementUtil")
@Slf4j
public class TaskManagementUtil {

    private final TaskmanagementApi taskmanagementApi;
    private final ObjectMapper objectMapper;

    public TaskManagementUtil(TaskmanagementApi taskmanagementApi, ObjectMapper objectMapper) {
        this.taskmanagementApi = taskmanagementApi;
        this.objectMapper = objectMapper;
    }

    public List<TaskManagement> searchTaskManagement(TaskSearchRequest request) {
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        try {
            org.pucar.dristi.caselifecycle.taskmanagement.internal.web.models.TaskSearchRequest tmRequest =
                    objectMapper.convertValue(
                            request,
                            org.pucar.dristi.caselifecycle.taskmanagement.internal.web.models.TaskSearchRequest.class);
            List<org.pucar.dristi.caselifecycle.taskmanagement.internal.web.models.TaskManagement> results =
                    taskmanagementApi.search(tmRequest);
            if (results == null || results.isEmpty()) {
                return Collections.emptyList();
            }
            return objectMapper.convertValue(
                    results,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, TaskManagement.class));
        } catch (Exception e) {
            log.error(SEARCHER_SERVICE_EXCEPTION, e);
            throw new CustomException("TASK_SEARCH_ERROR", "Error occurred while fetching task management records");
        }
    }
}
