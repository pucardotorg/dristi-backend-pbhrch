package org.pucar.dristi.caselifecycle.transformer.internal.event.impl;

import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.pucar.dristi.common.kafka.Producer;
import org.pucar.dristi.caselifecycle.transformer.internal.config.TransformerProperties;
import org.pucar.dristi.caselifecycle.transformer.internal.event.EventListener;
import org.pucar.dristi.caselifecycle.transformer.internal.models.CourtCase;
import org.pucar.dristi.caselifecycle.transformer.internal.models.Notification;
import org.pucar.dristi.caselifecycle.transformer.internal.models.OrderAndNotification;
import org.pucar.dristi.caselifecycle.transformer.internal.models.OrderNotificationRequest;
import org.pucar.dristi.caselifecycle.transformer.internal.service.CaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Map;

@Component
@Slf4j
public class NotificationImpl implements EventListener<Notification, RequestInfo> {

    private final Producer producer;
    private final TransformerProperties properties;
    private final CaseService caseService;

    @Autowired
    public NotificationImpl(Producer producer, TransformerProperties properties, CaseService caseService) {
        this.producer = producer;
        this.properties = properties;
        this.caseService = caseService;
    }

    @Override
    public void process(Notification event, RequestInfo requestInfo) {

        CourtCase courtCase =  null;
        courtCase = caseService.getCaseByCaseSearchText(event.getCaseNumber().get(0), event.getTenantId(), requestInfo);

        String businessOfTheDay = event.getAdditionalDetails() != null ? getBusinessOfTheDay(event.getAdditionalDetails()) : null;

        OrderAndNotification orderAndNotification = OrderAndNotification.builder()
                .type(event.getNotificationType())
                .id(event.getNotificationNumber())
                .courtId(courtCase.getCourtId())  // no court id
                .parties(new ArrayList<>())  // no parties
                .status(event.getStatus())
                .date(event.getCreatedDate())
                .entityType("Notification")
                .title(event.getNotificationType())
                .businessOfTheDay(businessOfTheDay)
                .tenantId(event.getTenantId())
                .filingNumbers( new ArrayList<>())
                .caseNumbers(event.getCaseNumber() != null ? event.getCaseNumber() : new ArrayList<>())
                .judgeIds( new ArrayList<>())
                .documents(event.getDocuments())
                .createdTime(event.getAuditDetails().getCreatedTime())
                .lastModifiedTime(event.getAuditDetails().getLastModifiedTime())
                .caseTitle(null)
                .caseSTNumber(null)
                .build();

        OrderNotificationRequest request = OrderNotificationRequest.builder()
                .requestInfo(requestInfo).orderAndNotification(orderAndNotification).build();

        producer.push(properties.getOrderAndNotificationTopic(), request);

    }

    private String getBusinessOfTheDay(Object additionalDetails) {
        if (additionalDetails instanceof Map) {
            Map<?, ?> detailsMap = (Map<?, ?>) additionalDetails;
            Object botd = detailsMap.get("businessOfTheDay");
            return botd != null ? botd.toString() : null;
        }
        return null;
    }

}
