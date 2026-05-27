package org.pucar.dristi.caselifecycle.casemanagement.internal.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.pucar.dristi.caselifecycle.task.TaskApi;
import org.pucar.dristi.common.contract.task.Task;
import org.pucar.dristi.common.contract.task.TaskCriteria;
import org.pucar.dristi.common.contract.task.TaskSearchRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class TaskSearchService {

    private final TaskApi taskApi;
    private final ObjectMapper objectMapper;

    @Autowired
    public TaskSearchService(TaskApi taskApi, ObjectMapper objectMapper) {
        this.taskApi = taskApi;
        this.objectMapper = objectMapper;
    }

    public ResponseEntity<Object> getTaskSearchResponse(String referenceId, String tenantId, RequestInfo requestInfo) {
        TaskSearchRequest request = new TaskSearchRequest();
        request.setRequestInfo(requestInfo);
        TaskCriteria criteria = new TaskCriteria();
        criteria.setId(referenceId);
        criteria.setTenantId(tenantId);
        request.setCriteria(criteria);

        try {
            List<Task> tasks = taskApi.search(request);
            // Preserve the original REST envelope so downstream callers
            // (controllers, etc.) see the same shape they used to over the
            // wire. `list` is the field the /task/v1/search controller
            // returns under in its response body.
            Map<String, Object> body = new HashMap<>();
            body.put("list", tasks != null ? tasks : java.util.Collections.emptyList());
            return ResponseEntity.ok(objectMapper.convertValue(body, Object.class));
        } catch (Exception e) {
            throw new CustomException("TASK_SEARCH_ERR", "error while fetching the task details:" + e.getMessage());
        }
    }
}
