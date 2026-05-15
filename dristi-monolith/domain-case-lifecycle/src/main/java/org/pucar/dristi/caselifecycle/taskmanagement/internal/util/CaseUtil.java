package org.pucar.dristi.caselifecycle.taskmanagement.internal.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.pucar.dristi.caselifecycle.cases.CaseApi;
import org.pucar.dristi.caselifecycle.taskmanagement.internal.web.models.CaseCriteria;
import org.pucar.dristi.caselifecycle.taskmanagement.internal.web.models.CaseSearchRequest;
import org.pucar.dristi.caselifecycle.taskmanagement.internal.web.models.TaskRequest;
import org.pucar.dristi.caselifecycle.taskmanagement.internal.web.models.cases.CourtCase;
import org.pucar.dristi.caselifecycle.taskmanagement.internal.web.models.cases.POAHolder;
import org.pucar.dristi.caselifecycle.taskmanagement.internal.web.models.cases.Party;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

import static org.pucar.dristi.caselifecycle.taskmanagement.internal.config.ServiceConstants.ERROR_FROM_CASE;
import static org.pucar.dristi.caselifecycle.taskmanagement.internal.config.ServiceConstants.ERROR_WHILE_FETCHING_FROM_CASE;


@Slf4j
@Component("taskmanagementCaseUtil")
@RequiredArgsConstructor
public class CaseUtil {

    private final CaseApi caseApi;
    private final ObjectMapper mapper;

    public JsonNode searchCaseDetails(CaseSearchRequest caseSearchRequest) {
        try {
            org.pucar.dristi.caselifecycle.cases.internal.web.models.CaseListResponse caseListResponse =
                    caseApi.search(buildCasesRequest(caseSearchRequest));

            List<org.pucar.dristi.caselifecycle.cases.internal.web.models.CaseCriteria> resultCriteria =
                    caseListResponse.getCriteria();
            if (resultCriteria == null || resultCriteria.isEmpty()
                    || resultCriteria.get(0).getResponseList() == null
                    || resultCriteria.get(0).getResponseList().isEmpty()) {
                throw new CustomException(ERROR_WHILE_FETCHING_FROM_CASE, "Invalid response structure from case service");
            }
            return mapper.readTree(mapper.writeValueAsString(resultCriteria.get(0).getResponseList().get(0)));
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error(ERROR_WHILE_FETCHING_FROM_CASE, e);
            throw new CustomException(ERROR_WHILE_FETCHING_FROM_CASE, e.getMessage());
        }
    }

    public List<CourtCase> getCaseDetails(TaskRequest taskRequest) {
        String filingNumber = taskRequest.getTask().getFilingNumber();
        RequestInfo requestInfo = taskRequest.getRequestInfo();

        CaseCriteria caseCriteria = CaseCriteria.builder().filingNumber(filingNumber)
                .defaultFields(false)
                .build();

        CaseSearchRequest caseSearchRequest = CaseSearchRequest.builder()
                .requestInfo(requestInfo)
                .criteria(Collections.singletonList(caseCriteria))
                .build();

        try {
            org.pucar.dristi.caselifecycle.cases.internal.web.models.CaseListResponse caseListResponse =
                    caseApi.search(buildCasesRequest(caseSearchRequest));
            log.info("Case response : {} ", caseListResponse);
            if (caseListResponse.getCriteria() == null || caseListResponse.getCriteria().isEmpty()) {
                return null;
            }
            List<?> responseList = caseListResponse.getCriteria().get(0).getResponseList();
            if (responseList == null) {
                return null;
            }
            return mapper.convertValue(
                    responseList,
                    mapper.getTypeFactory().constructCollectionType(List.class, CourtCase.class));
        } catch (Exception e) {
            log.error("Error while fetching from case service");
            throw new CustomException(ERROR_FROM_CASE, e.getMessage());
        }
    }

    private org.pucar.dristi.caselifecycle.cases.internal.web.models.CaseSearchRequest buildCasesRequest(CaseSearchRequest source) {
        List<org.pucar.dristi.caselifecycle.cases.internal.web.models.CaseCriteria> mapped = new ArrayList<>();
        if (source.getCriteria() != null) {
            for (CaseCriteria c : source.getCriteria()) {
                mapped.add(mapper.convertValue(c,
                        org.pucar.dristi.caselifecycle.cases.internal.web.models.CaseCriteria.class));
            }
        }
        return org.pucar.dristi.caselifecycle.cases.internal.web.models.CaseSearchRequest.builder()
                .requestInfo(source.getRequestInfo())
                .criteria(mapped)
                .build();
    }

    public Map<String, List<POAHolder>> getLitigantPoaMapping(CourtCase cases) {
        List<String> litigantIds = Optional.ofNullable(cases.getLitigants()).orElse(Collections.emptyList()).stream().filter(Party::getIsActive).map(Party::getIndividualId).filter(Objects::nonNull).toList();
        Map<String, List<POAHolder>> litigantPoaMapping = Optional.ofNullable(cases.getPoaHolders())
                .orElse(Collections.emptyList())
                .stream()
                .filter(POAHolder::getIsActive)
                .flatMap(poa -> {
                    // Create pairs of (litigantId, poa) for each litigant this POA represents
                    return poa.getRepresentingLitigants().stream()
                            .filter(party -> party.getIndividualId() != null)
                            .map(party -> new AbstractMap.SimpleEntry<>(party.getIndividualId(), poa));
                })
                .collect(Collectors.groupingBy(
                        Map.Entry::getKey,  // Group by litigant ID
                        Collectors.mapping(Map.Entry::getValue, Collectors.toList())
                ));

        for (String id : litigantIds) {
            litigantPoaMapping.putIfAbsent(id, new ArrayList<>()); // fill in missing ones with empty list
        }
        return litigantPoaMapping;
    }

    public List<Party> getRespondentOrComplainant(CourtCase caseDetails, String type) {
        return caseDetails.getLitigants()
                .stream()
                .filter(item -> item.getPartyType().contains(type))
                .collect(Collectors.toList());
    }

}