package org.pucar.dristi.integration.summons.internal.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.pucar.dristi.integration.summons.internal.service.DemandService;
import org.pucar.dristi.integration.summons.internal.service.SummonsService;
import org.pucar.dristi.integration.summons.internal.web.models.DeliveryStatus;
import org.pucar.dristi.integration.summons.internal.web.models.SummonsRequest;
import org.pucar.dristi.integration.summons.internal.web.models.TaskRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;

import java.util.HashMap;

import static org.pucar.dristi.integration.summons.internal.config.ServiceConstants.*;

@Component
@Slf4j
@EnableAsync
public class SummonsConsumer {

    private final SummonsService summonsService;

    private final ObjectMapper objectMapper;

    private final DemandService demandService;

    @Autowired
    public SummonsConsumer(SummonsService summonsService, ObjectMapper objectMapper, DemandService demandService) {
        this.summonsService = summonsService;
        this.objectMapper = objectMapper;
        this.demandService = demandService;
    }

    @KafkaListener(topics = {"${kafka.topic.save.task.application}"})
    @Async
    public void listenForGenerateSummonsDocument(final HashMap<String, Object> record, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        try {
            TaskRequest taskRequest = objectMapper.convertValue(record, TaskRequest.class);
            String taskType = taskRequest.getTask().getTaskType();
            String status = taskRequest.getTask().getStatus();

            // Process for generating summons bill
            boolean isValid = ((taskType.equalsIgnoreCase(SUMMON) || taskType.equalsIgnoreCase(NOTICE) || taskType.equalsIgnoreCase(WARRANT) || taskType.equalsIgnoreCase(PROCLAMATION) || taskType.equalsIgnoreCase(ATTACHMENT))
                    && PENDING_PAYMENT.equalsIgnoreCase(status));
            if (isValid) {
                try {
                    log.info("Received message for bill creation {}", taskRequest.getTask());
                    demandService.fetchPaymentDetailsAndGenerateDemandAndBill(taskRequest);
                } catch (Exception e) {
                    log.error("Error while creating bill: {}", taskRequest.getTask(), e);
                }
            }

            // Process for generating summons document
            if (isValid || (taskRequest.getTask().getWorkflow().getAction().equalsIgnoreCase(CREATE_WITH_OUT_PAYMENT))) {
                try {
                    log.info("Received message for uploading document {}", taskRequest.getTask());
                    summonsService.generateSummonsDocument(taskRequest);
                } catch (Exception e) {
                    log.error("Error while generating summons document: {}", taskRequest.getTask(), e);
                }
            }

            if (taskRequest.getTask().getWorkflow() !=null && taskRequest.getTask().getWorkflow().getAction().equalsIgnoreCase(CREATE) && ISSUE_PROCESS.equalsIgnoreCase(taskRequest.getTask().getStatus())) {
                try {
                    log.info("Received message for uploading document for miscellaneous process{}", taskRequest.getTask());
                    summonsService.generateMiscellaneousDocumentAndUpdateTask(taskRequest,false);
                } catch (Exception e) {
                    log.error("Error while generating miscellaneous process document : {}", taskRequest.getTask(), e);
                }
            }
        } catch (final Exception e) {
            log.error("Error while listening to value: {}: ", record, e);
        }
    }

    @KafkaListener(topics = {"${kafka.topic.update.summons}"})
    @Async
    public void listenForUpdateSummons(final HashMap<String, Object> record, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        try {
            SummonsRequest request = objectMapper.convertValue(record, SummonsRequest.class);
            log.info(request.toString());
            if (request.getSummonsDelivery().getDeliveryStatus().equals(DeliveryStatus.DELIVERED)
                    || request.getSummonsDelivery().getDeliveryStatus().equals(DeliveryStatus.NOT_DELIVERED)
                    || request.getSummonsDelivery().getDeliveryStatus().equals(DeliveryStatus.EXECUTED)
                    || request.getSummonsDelivery().getDeliveryStatus().equals(DeliveryStatus.NOT_EXECUTED)
                    || request.getSummonsDelivery().getDeliveryStatus().equals(DeliveryStatus.DELIVERED_ICOPS)
                    || request.getSummonsDelivery().getDeliveryStatus().equals(DeliveryStatus.NOT_DELIVERED_ICOPS)
                    || request.getSummonsDelivery().getDeliveryStatus().equals(DeliveryStatus.IN_TRANSIT)) {
                summonsService.updateTaskStatus(request);
            }
        } catch (final Exception e) {
            log.error("Error while listening to value: {}: ", record, e);
        }
    }

    @KafkaListener(topics = {"${kafka.topic.issue.summons.application}"})
    @Async
    public void listenForSendSummons(final HashMap<String, Object> record, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        try {
            TaskRequest taskRequest = objectMapper.convertValue(record, TaskRequest.class);
            log.info("Received message for sending summons {}", taskRequest.getTask());
            summonsService.sendSummonsViaChannels(taskRequest);
        } catch (final Exception e) {
            log.error("Error while listening to value: {}: ", record, e);
        }
    }
}
