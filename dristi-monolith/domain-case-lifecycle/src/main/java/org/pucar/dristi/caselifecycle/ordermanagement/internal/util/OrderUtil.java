package org.pucar.dristi.caselifecycle.ordermanagement.internal.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.pucar.dristi.common.order.OrderApi;
import org.pucar.dristi.caselifecycle.ordermanagement.internal.config.Configuration;
import org.pucar.dristi.common.contract.ordermanagement.*;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.pucar.dristi.caselifecycle.ordermanagement.internal.config.ServiceConstants.*;

@Component("ordermanagementOrderUtil")
@Slf4j
public class OrderUtil {

    private final Configuration configuration;
    private final ObjectMapper objectMapper;
    private final OrderApi orderApi;
    private final LocalizationUtil localizationUtil;

    @Autowired
    public OrderUtil(ObjectMapper objectMapper, Configuration configuration, OrderApi orderApi, LocalizationUtil localizationUtil) {
        this.configuration = configuration;
        this.objectMapper = objectMapper;
        this.orderApi = orderApi;
        this.localizationUtil = localizationUtil;
    }

    public Boolean fetchOrderDetails(OrderExistsRequest orderExistsRequest) {
        try {
            org.pucar.dristi.common.contract.order.OrderExistsRequest bridgedRequest =
                    objectMapper.convertValue(orderExistsRequest,
                            org.pucar.dristi.common.contract.order.OrderExistsRequest.class);
            List<org.pucar.dristi.common.contract.order.OrderExists> apiResult =
                    orderApi.exists(bridgedRequest);
            if (apiResult == null || apiResult.isEmpty()) {
                return Boolean.FALSE;
            }
            return apiResult.get(0).getExists();
        } catch (Exception e) {
            log.error(ERROR_WHILE_FETCHING_FROM_ORDER, e);
            throw new CustomException(ERROR_WHILE_FETCHING_FROM_ORDER, e.getMessage());
        }
    }

    public OrderResponse updateOrder(OrderRequest orderRequest) {
        try {
            org.pucar.dristi.common.contract.order.OrderRequest bridgedRequest =
                    objectMapper.convertValue(orderRequest,
                            org.pucar.dristi.common.contract.order.OrderRequest.class);
            org.pucar.dristi.common.contract.order.Order apiResult =
                    orderApi.update(bridgedRequest);
            return OrderResponse.builder()
                    .order(objectMapper.convertValue(apiResult, Order.class))
                    .build();
        } catch (Exception e) {
            log.error(ERROR_WHILE_FETCHING_FROM_ORDER, e);
            throw new CustomException(ERROR_WHILE_FETCHING_FROM_ORDER, e.getMessage());
        }
    }

    public OrderResponse createOrder(OrderRequest orderRequest) {
        try {
            org.pucar.dristi.common.contract.order.OrderRequest bridgedRequest =
                    objectMapper.convertValue(orderRequest,
                            org.pucar.dristi.common.contract.order.OrderRequest.class);
            org.pucar.dristi.common.contract.order.Order apiResult =
                    orderApi.create(bridgedRequest);
            return OrderResponse.builder()
                    .order(objectMapper.convertValue(apiResult, Order.class))
                    .build();
        } catch (Exception e) {
            log.error(ERROR_WHILE_FETCHING_FROM_ORDER, e);
            throw new CustomException(ERROR_WHILE_FETCHING_FROM_ORDER, e.getMessage());
        }
    }

    public OrderListResponse getOrders(OrderSearchRequest searchRequest) {
        try {
            org.pucar.dristi.common.contract.order.OrderSearchRequest bridgedRequest =
                    objectMapper.convertValue(searchRequest,
                            org.pucar.dristi.common.contract.order.OrderSearchRequest.class);
            org.pucar.dristi.common.contract.order.OrderListResponse apiResponse =
                    orderApi.search(bridgedRequest);
            return objectMapper.convertValue(apiResponse, OrderListResponse.class);
        } catch (Exception e) {
            log.error(ERROR_WHILE_FETCHING_FROM_ORDER, e);
            throw new CustomException(ERROR_WHILE_FETCHING_FROM_ORDER, e.getMessage());
        }
    }


    public String getReferenceId(Order order) {
        String referenceId = null;
        try {
            referenceId = (String) Optional.ofNullable(order)
                    .map(Order::getAdditionalDetails)
                    .filter(Map.class::isInstance)
                    .map(map -> (Map<?, ?>) map)
                    .map(map -> map.get("formdata"))
                    .filter(Map.class::isInstance)
                    .map(map -> (Map<?, ?>) map)
                    .map(map -> map.get("refApplicationId"))
                    .filter(String.class::isInstance).orElse(null);
        } catch (Exception e) {
            log.error("Error getting refApplicationId from order", e);
            throw new CustomException(ERROR_WHILE_FETCHING_FROM_ORDER, e.getMessage());
        }
        if (referenceId == null) {
            log.error("refApplicationId not found in order");
        }
        return referenceId;

    }

    public String getHearingNumberFormApplicationAdditionalDetails(Object additionalDetails) {
        return Optional.ofNullable(additionalDetails)
                .filter(Map.class::isInstance)
                .map(map -> (Map<?, ?>) map)
                .map(map -> map.get("hearingId"))
                .filter(String.class::isInstance)
                .map(String.class::cast).orElse(null);
    }

    public String getActionForApplication(Object additionalDetails) {

        String applicationStatus = Optional.ofNullable(additionalDetails)
                .filter(Map.class::isInstance)
                .map(map -> (Map<?, ?>) map)
                .map(map -> map.get("applicationStatus"))
                .filter(String.class::isInstance)
                .map(String.class::cast).orElseThrow(() -> new CustomException("", ""));

        return applicationStatusType(applicationStatus);


    }


    private String applicationStatusType(String type) {
        return switch (type) {
            case "APPROVED" -> APPROVE;
            case "SET_TERM_BAIL" -> SEND_BACK;
            default -> REJECT;
        };
    }

    public String getBusinessOfTheDay(Order order, RequestInfo requestInfo) {
        StringBuilder sb = new StringBuilder();

        try {
            // Attendance
            if (order.getAttendance() != null) {

                Object attendanceObj = order.getAttendance();

                Map<String, List<String>> attendanceMap = objectMapper.convertValue(
                        attendanceObj, new TypeReference<Map<String, List<String>>>() {
                        }
                );

                List<String> rolesLocalizedPresent = new ArrayList<>();
                List<String> rolesLocalizedAbsentee = new ArrayList<>();

                // Format and append
                for (Map.Entry<String, List<String>> entry : attendanceMap.entrySet()) {
                    String status = entry.getKey(); // "Present", "Absent"
                    List<String> roles = entry.getValue();

                    if("Present".equalsIgnoreCase(status)) {
                        if (roles != null) {
                            roles.forEach(role -> rolesLocalizedPresent.add(localizationUtil.callLocalization(requestInfo, order.getTenantId(), role)));
                        }
                    }
                    else {
                        if (roles != null) {
                            roles.forEach(role -> rolesLocalizedAbsentee.add(localizationUtil.callLocalization(requestInfo, order.getTenantId(), role)));
                        }
                    }
                }

                if (!rolesLocalizedPresent.isEmpty()) {
                    String linePresent = "Present" + ": " + String.join(", ", rolesLocalizedPresent);
                    sb.append(linePresent).append(DOT);
                }

                if(!rolesLocalizedAbsentee.isEmpty()){
                    String lineAbsent = "Absent" + ": " + String.join(", ", rolesLocalizedAbsentee);
                    sb.append(lineAbsent).append(DOT);
                }
            }

            // Item Text
            if (order.getItemText() != null) {
                String html = order.getItemText();
                String plainText = Jsoup.parse(html).text();
                sb.append(plainText).append(DOT);
            }

            // Purpose of Next Hearing
            if (order.getPurposeOfNextHearing() != null && !order.getPurposeOfNextHearing().isEmpty()) {
                String purpose = localizationUtil.callLocalization(requestInfo, order.getTenantId(), order.getPurposeOfNextHearing());
                sb.append("Purpose of Next Hearing: ")
                        .append(purpose).append(DOT);
            }

            // Next Hearing Date
            if (order.getNextHearingDate() != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                String dateStr = Instant.ofEpochMilli(order.getNextHearingDate())
                        .atZone(ZoneId.of(configuration.getZoneId()))
                        .toLocalDate()
                        .format(formatter);
                sb.append("Date of Next Hearing: ")
                        .append(dateStr).append(DOT);
            }

            return sb.toString().trim();

        } catch (Exception e) {
            log.error("Error extracting order text", e);
            throw new CustomException("Error extracting business of the day: ", "ERROR_BUSINESS_OF_THE_DAY");
        }
    }

    public String getHearingSummary(Order order, RequestInfo requestInfo) {
        StringBuilder sb = new StringBuilder();

        try {
            if (order.getAttendance() != null) {

                Object attendanceObj = order.getAttendance();

                Map<String, List<String>> attendanceMap = objectMapper.convertValue(
                        attendanceObj, new TypeReference<Map<String, List<String>>>() {
                        }
                );

                List<String> rolesLocalizedPresent = new ArrayList<>();
                List<String> rolesLocalizedAbsentee = new ArrayList<>();

                // Format and append
                for (Map.Entry<String, List<String>> entry : attendanceMap.entrySet()) {
                    String status = entry.getKey(); // "Present", "Absent"
                    List<String> roles = entry.getValue();

                    if("Present".equalsIgnoreCase(status)) {
                        if (roles != null) {
                            roles.forEach(role -> rolesLocalizedPresent.add(localizationUtil.callLocalization(requestInfo, order.getTenantId(), role)));
                        }
                    }
                    else {
                        if (roles != null) {
                            roles.forEach(role -> rolesLocalizedAbsentee.add(localizationUtil.callLocalization(requestInfo, order.getTenantId(), role)));
                        }
                    }
                }
                if (!rolesLocalizedPresent.isEmpty()) {
                    String linePresent = "Present" + ": " + String.join(", ", rolesLocalizedPresent);
                    sb.append(linePresent).append("\n");
                }

                if(!rolesLocalizedAbsentee.isEmpty()){
                    String lineAbsent = "Absent" + ": " + String.join(", ", rolesLocalizedAbsentee);
                    sb.append(lineAbsent).append("\n");
                }
            }
            // Item Text
            if (order.getItemText() != null) {
                String html = order.getItemText();
                String plainText = Jsoup.parse(html).text();
                sb.append(plainText).append("\n");
            }

            return sb.toString().trim();
        } catch (Exception e) {
            log.error("Error extracting order text", e);
            throw new CustomException("Error extracting business of the day: ", "ERROR_BUSINESS_OF_THE_DAY");
        }
    }


    public OrderResponse addOrderItem(@Valid OrderRequest request) {
        try {
            org.pucar.dristi.common.contract.order.OrderRequest bridgedRequest =
                    objectMapper.convertValue(request,
                            org.pucar.dristi.common.contract.order.OrderRequest.class);
            org.pucar.dristi.common.contract.order.Order apiResult =
                    orderApi.addOrderItem(bridgedRequest);
            return OrderResponse.builder()
                    .order(objectMapper.convertValue(apiResult, Order.class))
                    .build();
        } catch (Exception e) {
            log.error(ERROR_WHILE_FETCHING_FROM_ORDER, e);
            throw new CustomException(ERROR_WHILE_FETCHING_FROM_ORDER, e.getMessage());
        }
    }
}
