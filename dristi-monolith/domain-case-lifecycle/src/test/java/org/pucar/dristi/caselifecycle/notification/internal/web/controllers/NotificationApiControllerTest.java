package org.pucar.dristi.caselifecycle.notification.internal.web.controllers;

import org.egov.common.contract.request.RequestInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pucar.dristi.caselifecycle.notification.internal.service.NotificationService;
import org.pucar.dristi.common.contract.notification.*;
import org.pucar.dristi.common.util.ResponseInfoFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class NotificationApiControllerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private ResponseInfoFactory responseInfoFactory;

    @InjectMocks
    private NotificationApiController controller;

    @Test
    public void notificationV1CreateReturnsOk() {
        NotificationRequest request = NotificationRequest.builder()
                .requestInfo(new RequestInfo())
                .build();
        Notification notification = mock(Notification.class);
        when(notificationService.createV1Notification(request)).thenReturn(notification);

        ResponseEntity<NotificationResponse> response = controller.notificationV1Create(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(notification, response.getBody().getNotification());
    }

    @Test
    public void notificationV1ExistsReturnsOk() {
        NotificationExistsRequest request = NotificationExistsRequest.builder()
                .requestInfo(new RequestInfo())
                .build();
        List<NotificationExists> existsList = Collections.emptyList();
        when(notificationService.existV1Notification(request)).thenReturn(existsList);

        ResponseEntity<NotificationExistsResponse> response = controller.notificationV1Exists(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(existsList, response.getBody().getNotificationList());
    }

    @Test
    public void notificationV1SearchReturnsOk() {
        NotificationSearchRequest request = NotificationSearchRequest.builder()
                .requestInfo(new RequestInfo())
                .build();
        List<Notification> notifications = Collections.emptyList();
        when(notificationService.searchV1Notification(request)).thenReturn(notifications);

        ResponseEntity<NotificationListResponse> response = controller.notificationV1Search(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(notifications, response.getBody().getList());
    }

    @Test
    public void notificationV1UpdateReturnsOk() {
        NotificationRequest request = NotificationRequest.builder()
                .requestInfo(new RequestInfo())
                .build();
        Notification notification = mock(Notification.class);
        when(notificationService.updateV1Notification(request)).thenReturn(notification);

        ResponseEntity<NotificationResponse> response = controller.notificationV1Update(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(notification, response.getBody().getNotification());
    }
}
