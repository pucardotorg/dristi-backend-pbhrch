package org.pucar.dristi.caselifecycle.task.internal.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.pucar.dristi.caselifecycle.cases.CaseApi;
import org.pucar.dristi.common.contract.task.CourtCase;
import org.pucar.dristi.common.contract.task.POAHolder;
import org.pucar.dristi.common.contract.task.Party;
import org.pucar.dristi.common.contract.task.TaskRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.pucar.dristi.caselifecycle.task.internal.config.ServiceConstants.ERROR_FROM_CASE;
import static org.pucar.dristi.caselifecycle.task.internal.config.ServiceConstants.ERROR_WHILE_FETCHING_FROM_CASE;

@Slf4j
@Component("taskCaseUtil")
public class CaseUtil {

    private final CaseApi caseApi;
    private final ObjectMapper objectMapper;

    @Autowired
    public CaseUtil(@Lazy CaseApi caseApi, ObjectMapper objectMapper) {
        this.caseApi = caseApi;
        this.objectMapper = objectMapper;
    }

    public JsonNode searchCaseDetails(RequestInfo requestInfo, String tenantId, String cnrNumber, String filingNumber, String caseId) {
        org.pucar.dristi.caselifecycle.cases.internal.web.models.CaseCriteria criteria =
                org.pucar.dristi.caselifecycle.cases.internal.web.models.CaseCriteria.builder()
                        .cnrNumber(cnrNumber)
                        .filingNumber(filingNumber)
                        .caseId(caseId)
                        .build();
        org.pucar.dristi.caselifecycle.cases.internal.web.models.CaseSearchRequest request =
                org.pucar.dristi.caselifecycle.cases.internal.web.models.CaseSearchRequest.builder()
                        .requestInfo(requestInfo)
                        .criteria(Collections.singletonList(criteria))
                        .build();
        try {
            caseApi.search(request);
            var resultCriteria = request.getCriteria();
            if (resultCriteria == null || resultCriteria.isEmpty()
                    || resultCriteria.get(0).getResponseList() == null) {
                throw new CustomException(ERROR_WHILE_FETCHING_FROM_CASE, "Invalid response structure");
            }
            return objectMapper.readTree(objectMapper.writeValueAsString(resultCriteria.get(0).getResponseList()));
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error(ERROR_WHILE_FETCHING_FROM_CASE, e);
            throw new CustomException(ERROR_WHILE_FETCHING_FROM_CASE, e.getMessage());
        }
    }

    public List<CourtCase> getCaseDetails(TaskRequest taskRequest) {
        org.pucar.dristi.caselifecycle.cases.internal.web.models.CaseCriteria criteria =
                org.pucar.dristi.caselifecycle.cases.internal.web.models.CaseCriteria.builder()
                        .filingNumber(taskRequest.getTask().getFilingNumber())
                        .defaultFields(false)
                        .build();
        org.pucar.dristi.caselifecycle.cases.internal.web.models.CaseSearchRequest request =
                org.pucar.dristi.caselifecycle.cases.internal.web.models.CaseSearchRequest.builder()
                        .requestInfo(taskRequest.getRequestInfo())
                        .criteria(Collections.singletonList(criteria))
                        .build();
        try {
            caseApi.search(request);
            var resultCriteria = request.getCriteria();
            if (resultCriteria == null || resultCriteria.isEmpty()
                    || resultCriteria.get(0).getResponseList() == null) {
                return null;
            }
            return objectMapper.convertValue(
                    resultCriteria.get(0).getResponseList(),
                    new TypeReference<List<CourtCase>>() {});
        } catch (Exception e) {
            log.error("Error while fetching from case service", e);
            throw new CustomException(ERROR_FROM_CASE, e.getMessage());
        }
    }

    public void editCase(RequestInfo requestInfo, CourtCase courtCase) {
        try {
            org.pucar.dristi.caselifecycle.cases.internal.web.models.CourtCase casesCourtCase =
                    objectMapper.convertValue(courtCase,
                            org.pucar.dristi.caselifecycle.cases.internal.web.models.CourtCase.class);
            org.pucar.dristi.caselifecycle.cases.internal.web.models.CaseRequest request =
                    org.pucar.dristi.caselifecycle.cases.internal.web.models.CaseRequest.builder()
                            .requestInfo(requestInfo)
                            .cases(casesCourtCase)
                            .build();
            caseApi.edit(request);
        } catch (Exception e) {
            log.error("Error while editing case", e);
            throw new CustomException(ERROR_FROM_CASE, e.getMessage());
        }
    }

    public List<Party> getRespondentOrComplainant(CourtCase caseDetails, String type) {
        return caseDetails.getLitigants()
                .stream()
                .filter(item -> item.getPartyType().contains(type))
                .collect(Collectors.toList());
    }

    public Map<String, List<POAHolder>> getLitigantPoaMapping(CourtCase cases) {
        List<String> litigantIds = Optional.ofNullable(cases.getLitigants()).orElse(Collections.emptyList()).stream().filter(Party::getIsActive).map(Party::getIndividualId).filter(Objects::nonNull).toList();
        Map<String, List<POAHolder>> litigantPoaMapping = Optional.ofNullable(cases.getPoaHolders())
                .orElse(Collections.emptyList())
                .stream()
                .filter(POAHolder::getIsActive)
                .flatMap(poa -> poa.getRepresentingLitigants().stream()
                        .filter(party -> party.getIndividualId() != null)
                        .map(party -> new AbstractMap.SimpleEntry<>(party.getIndividualId(), poa)))
                .collect(Collectors.groupingBy(
                        Map.Entry::getKey,
                        Collectors.mapping(Map.Entry::getValue, Collectors.toList())
                ));

        for (String id : litigantIds) {
            litigantPoaMapping.putIfAbsent(id, new ArrayList<>());
        }
        return litigantPoaMapping;
    }
}
