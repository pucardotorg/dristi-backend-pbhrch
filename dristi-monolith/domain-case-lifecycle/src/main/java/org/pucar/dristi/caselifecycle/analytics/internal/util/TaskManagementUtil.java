package org.pucar.dristi.caselifecycle.analytics.internal.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.tracer.model.CustomException;
import org.pucar.dristi.caselifecycle.analytics.internal.web.models.taskManagement.TaskManagement;
import org.pucar.dristi.caselifecycle.analytics.internal.web.models.taskManagement.TaskSearchRequest;
import org.pucar.dristi.caselifecycle.taskmanagement.TaskmanagementApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component("analyticsTaskManagementUtil")
@Slf4j
public class TaskManagementUtil {

    private final TaskmanagementApi taskmanagementApi;
    private final ObjectMapper objectMapper;

    @Autowired
    public TaskManagementUtil(TaskmanagementApi taskmanagementApi, ObjectMapper objectMapper) {
        this.taskmanagementApi = taskmanagementApi;
        this.objectMapper = objectMapper;
    }

    public List<TaskManagement> searchTaskManagement(TaskSearchRequest request) {
        try {
            org.pucar.dristi.caselifecycle.taskmanagement.internal.web.models.TaskSearchRequest bridged =
                    objectMapper.convertValue(request,
                            org.pucar.dristi.caselifecycle.taskmanagement.internal.web.models.TaskSearchRequest.class);
            List<org.pucar.dristi.caselifecycle.taskmanagement.internal.web.models.TaskManagement> records =
                    taskmanagementApi.search(bridged);
            if (records == null || records.isEmpty()) {
                return Collections.emptyList();
            }
            return objectMapper.convertValue(records, new TypeReference<List<TaskManagement>>() {});
        } catch (Exception e) {
            log.error("Error occurred while fetching task management records", e);
            throw new CustomException("TASK_SEARCH_ERROR", "Error occurred while fetching task management records");
        }
    }

}
