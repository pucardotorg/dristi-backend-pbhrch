package org.pucar.dristi.payments.calculator.internal.payment.calculator.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.pucar.dristi.caselifecycle.cases.CaseApi;
import org.pucar.dristi.caselifecycle.cases.internal.web.models.CaseCriteria;
import org.pucar.dristi.caselifecycle.cases.internal.web.models.CaseListResponse;
import org.pucar.dristi.caselifecycle.cases.internal.web.models.CaseSearchRequest;
import org.pucar.dristi.caselifecycle.cases.internal.web.models.CourtCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static org.pucar.dristi.payments.calculator.internal.payment.calculator.config.ServiceConstants.ERROR_WHILE_FETCHING_FROM_CASE;
import static org.pucar.dristi.payments.calculator.internal.payment.calculator.config.ServiceConstants.FLOW_JAC;

@Component("calculatorCaseUtil")
@Slf4j
public class CaseUtil {

    private final CaseApi caseApi;
    private final ObjectMapper mapper;

    @Autowired
    public CaseUtil(CaseApi caseApi, ObjectMapper mapper) {
        this.caseApi = caseApi;
        this.mapper = mapper;
    }

    public Map<String, List<JsonNode>> getAdvocateForLitigant(RequestInfo requestInfo, String filingNumber, String tenantId) {
        CaseCriteria criteria = CaseCriteria.builder()
                .filingNumber(filingNumber)
                .defaultFields(false)
                .build();
        CaseSearchRequest searchRequest = CaseSearchRequest.builder()
                .requestInfo(requestInfo)
                .flow(FLOW_JAC)
                .criteria(Collections.singletonList(criteria)).build();

        JsonNode caseNode = firstCaseAsJson(searchRequest);

        JsonNode representatives = caseNode.get("representatives");
        JsonNode litigants = caseNode.get("litigants");

        Set<String> litigantIds = Optional.ofNullable(litigants)
                .map(litigant -> StreamSupport.stream(litigant.spliterator(), false)
                        .map(litigantNode -> litigantNode.get("individualId"))
                        .filter(Objects::nonNull)
                        .map(JsonNode::asText)
                        .collect(Collectors.toSet()))
                .orElse(Collections.emptySet());

        Map<String, List<JsonNode>> representativesMap = Optional.ofNullable(representatives)
                .map(repNodes -> StreamSupport.stream(repNodes.spliterator(), false))
                .orElse(Stream.empty())
                .filter(repNode -> repNode.has("isActive") && repNode.get("isActive").asBoolean())
                .flatMap(repNode -> {
                    JsonNode representing = repNode.get("representing");
                    if (representing == null || !representing.isArray()) return Stream.empty();

                    return StreamSupport.stream(representing.spliterator(), false)
                            .map(repLitigantNode -> repLitigantNode.get("individualId"))
                            .filter(Objects::nonNull)
                            .map(JsonNode::asText)
                            .filter(litigantIds::contains)
                            .map(litigantId -> new AbstractMap.SimpleEntry<>(litigantId, repNode));
                })
                .collect(Collectors.groupingBy(Map.Entry::getKey,
                        Collectors.mapping(Map.Entry::getValue, Collectors.toList())));

        log.info("filingNumber={}, representatives={}, representativesMap size={}", filingNumber, representatives, representativesMap.size());
        return representativesMap;
    }

    private JsonNode firstCaseAsJson(CaseSearchRequest searchRequest) {
        try {
            CaseListResponse response = caseApi.search(searchRequest);
            List<CourtCase> responseList = response.getCriteria().get(0).getResponseList();
            return mapper.valueToTree(responseList.get(0));
        } catch (Exception e) {
            log.error(ERROR_WHILE_FETCHING_FROM_CASE, e);
            throw new CustomException(ERROR_WHILE_FETCHING_FROM_CASE, e.getMessage());
        }
    }
}
