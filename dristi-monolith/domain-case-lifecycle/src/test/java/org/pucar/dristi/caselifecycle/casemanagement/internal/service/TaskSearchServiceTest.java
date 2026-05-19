package org.pucar.dristi.caselifecycle.casemanagement.internal.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pucar.dristi.caselifecycle.task.TaskApi;
import org.pucar.dristi.common.contract.task.Task;
import org.pucar.dristi.common.contract.task.TaskSearchRequest;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskSearchServiceTest {

    @Mock
    private TaskApi taskApi;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private TaskSearchService taskSearchService;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        taskSearchService = new TaskSearchService(taskApi, objectMapper);
    }

    @Test
    void testGetTaskSearchResponseSuccess() {
        RequestInfo requestInfo = new RequestInfo();
        requestInfo.setAuthToken("auth-token");

        Task task = new Task();
        when(taskApi.search(any(TaskSearchRequest.class))).thenReturn(List.of(task));

        ResponseEntity<Object> response = taskSearchService.getTaskSearchResponse("123", "tenant1", requestInfo);

        assertNotNull(response);
        assertTrue(response.getStatusCode().is2xxSuccessful());
        verify(taskApi, times(1)).search(any(TaskSearchRequest.class));
    }

    @Test
    void testGetTaskSearchResponseThrowsCustomException() {
        RequestInfo requestInfo = new RequestInfo();
        requestInfo.setAuthToken("auth-token");

        when(taskApi.search(any(TaskSearchRequest.class)))
                .thenThrow(new RuntimeException("TaskApi Exception"));

        CustomException exception = assertThrows(CustomException.class, () ->
                taskSearchService.getTaskSearchResponse("123", "tenant1", requestInfo));

        assertEquals("TASK_SEARCH_ERR", exception.getCode());
        assertTrue(exception.getMessage().contains("error while fetching the task details"));
    }

    @Test
    void testGetTaskSearchResponseEmptyList() {
        RequestInfo requestInfo = new RequestInfo();
        requestInfo.setAuthToken("auth-token");

        when(taskApi.search(any(TaskSearchRequest.class))).thenReturn(java.util.Collections.emptyList());

        ResponseEntity<Object> response = taskSearchService.getTaskSearchResponse("123", "tenant1", requestInfo);

        assertNotNull(response);
        assertTrue(response.getStatusCode().is2xxSuccessful());
    }
}
