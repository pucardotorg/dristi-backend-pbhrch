package org.pucar.dristi.caselifecycle.ordermanagement.internal.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.models.Document;
import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.springframework.stereotype.Component;
import org.pucar.dristi.caselifecycle.task.TaskApi;
import org.pucar.dristi.common.util.DateUtil;
import org.pucar.dristi.common.contract.ordermanagement.Order;
import org.pucar.dristi.common.models.workflow.WorkflowObject;
import org.pucar.dristi.caselifecycle.ordermanagement.internal.web.models.courtCase.CourtCase;
import org.pucar.dristi.caselifecycle.ordermanagement.internal.web.models.task.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.pucar.dristi.caselifecycle.ordermanagement.internal.config.ServiceConstants.*;

@Component("ordermanagementTaskUtil")
@Slf4j
public class TaskUtil {

    private final TaskApi taskApi;
    private final ObjectMapper objectMapper;
    private final DateUtil dateUtil;
    private final JsonUtil jsonUtil;

    public TaskUtil(TaskApi taskApi, ObjectMapper objectMapper, DateUtil dateUtil, JsonUtil jsonUtil) {
        this.taskApi = taskApi;
        this.objectMapper = objectMapper;
        this.dateUtil = dateUtil;
        this.jsonUtil = jsonUtil;
    }

    public TaskResponse callCreateTask(TaskRequest taskRequest) {
        try {
            org.pucar.dristi.common.contract.task.TaskRequest bridgedRequest =
                    objectMapper.convertValue(taskRequest,
                            org.pucar.dristi.common.contract.task.TaskRequest.class);
            org.pucar.dristi.common.contract.task.Task apiResult = taskApi.create(bridgedRequest);
            return TaskResponse.builder()
                    .task(objectMapper.convertValue(apiResult, Task.class))
                    .build();
        } catch (Exception e) {
            log.error("Error getting response from Task Service", e);
            throw new CustomException("TASK_CREATE_ERROR", "Error getting response from task Service");
        }
    }

    public TaskListResponse searchTask(TaskSearchRequest request) {
        try {
            org.pucar.dristi.common.contract.task.TaskSearchRequest bridgedRequest =
                    objectMapper.convertValue(request,
                            org.pucar.dristi.common.contract.task.TaskSearchRequest.class);
            List<org.pucar.dristi.common.contract.task.Task> apiResult = taskApi.search(bridgedRequest);
            List<Task> tasks = apiResult == null ? Collections.emptyList() :
                    apiResult.stream()
                            .map(t -> objectMapper.convertValue(t, Task.class))
                            .toList();
            return TaskListResponse.builder()
                    .list(tasks)
                    .totalCount(tasks.size())
                    .build();
        } catch (Exception e) {
            log.error(SEARCHER_SERVICE_EXCEPTION, e);
            throw new CustomException();
        }
    }

    public TaskResponse updateTask(TaskRequest request) {
        try {
            org.pucar.dristi.common.contract.task.TaskRequest bridgedRequest =
                    objectMapper.convertValue(request,
                            org.pucar.dristi.common.contract.task.TaskRequest.class);
            org.pucar.dristi.common.contract.task.Task apiResult = taskApi.update(bridgedRequest);
            return TaskResponse.builder()
                    .task(objectMapper.convertValue(apiResult, Task.class))
                    .build();
        } catch (Exception e) {
            log.error(SEARCHER_SERVICE_EXCEPTION, e);
            throw new CustomException();
        }
    }


    public TaskRequest createTaskRequest(RequestInfo requestInfo, Order order, Object taskDetails, CourtCase courtCase, String channel) {
        String itemId = jsonUtil.getNestedValue(order.getAdditionalDetails(), List.of("itemId"), String.class);

        Map<String, Object> additionalDetails = new HashMap<>();
        if (itemId!= null){
            additionalDetails.put("itemId",itemId);
        }

        WorkflowObject workflowObject = new WorkflowObject();
        if (EMAIL.equalsIgnoreCase(channel) || SMS.equalsIgnoreCase(channel) || courtCase.getIsLPRCase() ||
                isCourtWitness(order.getOrderType(), objectMapper.convertValue(taskDetails, JsonNode.class))) {
            workflowObject.setAction("CREATE_WITH_OUT_PAYMENT");
            // There is no pending collection when payment is not made
            ObjectNode taskDetailsNode = (ObjectNode) taskDetails;
            ObjectNode deliveryChannels = (ObjectNode) taskDetailsNode.get("deliveryChannels");
            if (deliveryChannels == null) {
                deliveryChannels = objectMapper.createObjectNode();
                taskDetailsNode.set("deliveryChannels", deliveryChannels);
            }
            deliveryChannels.put("isPendingCollection", false);
        }
        else {
            workflowObject.setAction("CREATE");
        }
        workflowObject.setComments(order.getOrderType());
        workflowObject.setDocuments(Collections.singletonList(Document.builder().build()));

        Task task = Task.builder()
                .tenantId(order.getTenantId())
                .orderId(order.getId())
                .filingNumber(order.getFilingNumber())
                .cnrNumber(order.getCnrNumber())
                .createdDate(dateUtil.getCurrentTimeInMilis())
                .taskType(order.getOrderType())
                .caseId(courtCase.getId().toString())
                .caseTitle(courtCase.getCaseTitle())
                .taskDetails(taskDetails)
                .amount(Amount.builder().type("FINE").status("DONE").amount("0").build()) // here amount need to fetch from somewhere
                .status("INPROGRESS")
                .additionalDetails(additionalDetails) // here new hashmap
                .workflow(workflowObject)
                .build();

         return TaskRequest.builder().requestInfo(requestInfo).task(task).build();
    }

    public boolean isCourtWitness(String orderType, JsonNode taskDetails) {
        if(Set.of(WARRANT, PROCLAMATION, ATTACHMENT).contains(orderType.toUpperCase())){
            return taskDetails.get("respondentDetails")!=null && (taskDetails.get("respondentDetails").get("ownerType") != null &&
                    taskDetails.get("respondentDetails").get("ownerType").textValue().equalsIgnoreCase(COURT_WITNESS));
        } if(SUMMONS.equalsIgnoreCase(orderType)) {
            return taskDetails.get("witnessDetails") != null && (taskDetails.get("witnessDetails").get("ownerType") == null ||
                    taskDetails.get("witnessDetails").get("ownerType").textValue().equalsIgnoreCase(COURT_WITNESS));
        }
        return false;
    }

    /**
     * Creates a TaskRequest for WARRANT order type with upfront payment check.
     * If hasUpfrontPayment is true, uses CREATE action (requires payment).
     * If hasUpfrontPayment is false, uses CREATE_WITH_OUT_PAYMENT action.
     */
    public TaskRequest createWarrantTaskRequest(RequestInfo requestInfo, Order order, Object taskDetails,
                                                 CourtCase courtCase, String channel, boolean hasUpfrontPayment) {

        String itemId = jsonUtil.getNestedValue(order.getAdditionalDetails(), List.of("itemId"), String.class);

        Map<String, Object> additionalDetails = new HashMap<>();
        if (itemId != null) {
            additionalDetails.put("itemId", itemId);
        }

        WorkflowObject workflowObject = new WorkflowObject();
        JsonNode taskDetailsNode = objectMapper.convertValue(taskDetails, JsonNode.class);

        // Determine workflow action based on upfront payment status
        // hasUpfrontPayment=true means payment was done upfront, so no payment required now
        // hasUpfrontPayment=false means no upfront payment found, so payment is required
        if (EMAIL.equalsIgnoreCase(channel) || SMS.equalsIgnoreCase(channel) || courtCase.getIsLPRCase() ||
                isCourtWitness(order.getOrderType(), taskDetailsNode) || hasUpfrontPayment) {
            workflowObject.setAction("CREATE_WITH_OUT_PAYMENT");
            log.info("Creating warrant task without payment - channel: {}, hasUpfrontPayment: {}", channel, hasUpfrontPayment);
            // There is no pending collection when payment is not made
            ObjectNode taskDetailsObjNode = (ObjectNode) taskDetails;
            ObjectNode deliveryChannels = (ObjectNode) taskDetailsObjNode.get("deliveryChannels");
            if (deliveryChannels == null) {
                deliveryChannels = objectMapper.createObjectNode();
                taskDetailsObjNode.set("deliveryChannels", deliveryChannels);
            }
            deliveryChannels.put("isPendingCollection", false);
            if (hasUpfrontPayment) {
                LocalDate feePaidDate = dateUtil.getLocalDateFromEpoch(courtCase.getFilingDate());
                deliveryChannels.put("feePaidDate", feePaidDate.format(DateTimeFormatter.ofPattern(LOCAL_DATE_FORMAT)));
                if (RPAD.equalsIgnoreCase(channel)) {
                    deliveryChannels.put("isPendingCollection", true);
                }

            }
        } else {
            workflowObject.setAction("CREATE");
            log.info("Creating warrant task with payment - channel: {}, hasUpfrontPayment: {}", channel, hasUpfrontPayment);
        }
        workflowObject.setComments(order.getOrderType());
        workflowObject.setDocuments(Collections.singletonList(Document.builder().build()));

        Task task = Task.builder()
                .tenantId(order.getTenantId())
                .orderId(order.getId())
                .filingNumber(order.getFilingNumber())
                .cnrNumber(order.getCnrNumber())
                .createdDate(dateUtil.getCurrentTimeInMilis())
                .taskType(order.getOrderType())
                .caseId(courtCase.getId().toString())
                .caseTitle(courtCase.getCaseTitle())
                .taskDetails(taskDetails)
                .amount(Amount.builder().type("FINE").status("DONE").amount("0").build())
                .status("INPROGRESS")
                .additionalDetails(additionalDetails)
                .workflow(workflowObject)
                .build();

        return TaskRequest.builder().requestInfo(requestInfo).task(task).build();
    }

    public String constructFullName(String firstName, String middleName, String lastName) {
        return Stream.of(firstName, middleName, lastName)
                .filter(name -> name != null && !name.isEmpty()) // Remove null and empty values
                .collect(Collectors.joining(" ")) // Join with space
                .trim();
    }

    public String getFormattedName(String firstName, String middleName, String lastName, String designation, String partyTypeLabel) {
        // Build the name parts while filtering out null/empty values
        String nameParts = Stream.of(firstName, middleName, lastName)
                .filter(name -> name != null && !name.isEmpty())
                .collect(Collectors.joining(" "));

        // Handle designation
        String nameWithDesignation = (designation != null && !designation.isEmpty() && !nameParts.isEmpty())
                ? nameParts + " - " + designation
                : (designation != null && !designation.isEmpty()) ? designation : nameParts;

        // Handle party type label
        return (partyTypeLabel != null && !partyTypeLabel.isEmpty())
                ? nameWithDesignation + " " + partyTypeLabel
                : nameWithDesignation;
    }
}
