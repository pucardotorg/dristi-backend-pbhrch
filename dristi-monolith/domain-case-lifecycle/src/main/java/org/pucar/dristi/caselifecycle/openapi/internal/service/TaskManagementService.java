package org.pucar.dristi.caselifecycle.openapi.internal.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.pucar.dristi.caselifecycle.openapi.internal.config.Configuration;
import org.pucar.dristi.caselifecycle.openapi.internal.web.models.task_management.TaskManagement;
import org.pucar.dristi.caselifecycle.openapi.internal.web.models.task_management.TaskManagementRequest;
import org.pucar.dristi.caselifecycle.openapi.internal.web.models.task_management.TaskManagementResponse;
import org.pucar.dristi.caselifecycle.openapi.internal.web.models.task_management.TaskManagementSearchResponse;
import org.pucar.dristi.caselifecycle.openapi.internal.web.models.task_management.TaskSearchRequest;
import org.pucar.dristi.caselifecycle.taskmanagement.TaskmanagementApi;
import org.pucar.dristi.common.repository.ServiceRequestRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("openapiTaskManagementService")
@Slf4j
public class TaskManagementService {

    private final Configuration config;
    private final ServiceRequestRepository serviceRequestRepository;
    private final ObjectMapper objectMapper;
    private final TaskmanagementApi taskmanagementApi;

    public TaskManagementService(Configuration config, ServiceRequestRepository serviceRequestRepository,
                                 ObjectMapper objectMapper, TaskmanagementApi taskmanagementApi) {
        this.config = config;
        this.serviceRequestRepository = serviceRequestRepository;
        this.objectMapper = objectMapper;
        this.taskmanagementApi = taskmanagementApi;
    }

    // Rule 35: writes stay REST until cross-subdomain write semantics are designed.
    public TaskManagementResponse createTaskManagement(TaskManagementRequest taskManagementRequest) {
        StringBuilder uri = new StringBuilder();
        uri.append(config.getTaskManagementHost()).append(config.getTaskManagementCreateEndpoint());
        log.info("method=createTaskManagement, status=IN_PROGRESS, request={}", taskManagementRequest);

        Object response = serviceRequestRepository.fetchResult(uri, taskManagementRequest);
        log.info("method=createTaskManagement, status=SUCCESS");

        return objectMapper.convertValue(response, TaskManagementResponse.class);
    }

    public TaskManagementResponse updateTaskManagement(TaskManagementRequest taskManagementRequest) {
        log.info("method=updateTaskManagement, status=IN_PROGRESS, request={}", taskManagementRequest);
        org.pucar.dristi.caselifecycle.taskmanagement.internal.web.models.TaskManagementRequest bridged =
                objectMapper.convertValue(taskManagementRequest,
                        org.pucar.dristi.caselifecycle.taskmanagement.internal.web.models.TaskManagementRequest.class);
        org.pucar.dristi.caselifecycle.taskmanagement.internal.web.models.TaskManagementResponse response =
                taskmanagementApi.update(bridged);
        log.info("method=updateTaskManagement, status=SUCCESS");
        return objectMapper.convertValue(response, TaskManagementResponse.class);
    }

    public TaskManagementSearchResponse searchTaskManagement(TaskSearchRequest taskSearchRequest) {
        log.info("method=searchTaskManagement, status=IN_PROGRESS, request={}", taskSearchRequest);
        org.pucar.dristi.caselifecycle.taskmanagement.internal.web.models.TaskSearchRequest bridged =
                objectMapper.convertValue(taskSearchRequest,
                        org.pucar.dristi.caselifecycle.taskmanagement.internal.web.models.TaskSearchRequest.class);
        List<org.pucar.dristi.caselifecycle.taskmanagement.internal.web.models.TaskManagement> records =
                taskmanagementApi.search(bridged);
        log.info("method=searchTaskManagement, status=SUCCESS");

        TaskManagementSearchResponse searchResponse = new TaskManagementSearchResponse();
        searchResponse.setTaskManagementRecords(
                objectMapper.convertValue(records, new TypeReference<List<TaskManagement>>() {}));
        searchResponse.setTotalCount(records == null ? 0 : records.size());
        return searchResponse;
    }
}
