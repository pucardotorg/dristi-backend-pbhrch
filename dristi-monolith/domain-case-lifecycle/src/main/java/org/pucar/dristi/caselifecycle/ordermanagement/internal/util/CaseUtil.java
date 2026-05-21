package org.pucar.dristi.caselifecycle.ordermanagement.internal.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.pucar.dristi.caselifecycle.cases.CaseApi;
import org.pucar.dristi.caselifecycle.ordermanagement.internal.web.models.courtCase.*;

import java.util.*;
import java.util.stream.Collectors;

import static org.pucar.dristi.caselifecycle.ordermanagement.internal.config.ServiceConstants.ERROR_WHILE_FETCHING_FROM_CASE;
import static org.pucar.dristi.caselifecycle.ordermanagement.internal.config.ServiceConstants.SEARCHER_SERVICE_EXCEPTION;

@Component("ordermanagementCaseUtil")
@Slf4j
public class CaseUtil {

    private final ObjectMapper objectMapper;
    private final CaseApi caseApi;
    private final CacheUtil cacheUtil;

    @Autowired
    public CaseUtil(ObjectMapper objectMapper, CaseApi caseApi, CacheUtil cacheUtil) {
        this.objectMapper = objectMapper;
        this.caseApi = caseApi;
        this.cacheUtil = cacheUtil;
    }

    public CaseListResponse searchCaseDetails(CaseSearchRequest caseSearchRequest) {
        try {
            org.pucar.dristi.caselifecycle.cases.internal.web.models.CaseSearchRequest bridgedRequest =
                    objectMapper.convertValue(caseSearchRequest,
                            org.pucar.dristi.caselifecycle.cases.internal.web.models.CaseSearchRequest.class);
            org.pucar.dristi.caselifecycle.cases.internal.web.models.CaseListResponse apiResponse =
                    caseApi.search(bridgedRequest);
            return objectMapper.convertValue(apiResponse, CaseListResponse.class);
        } catch (Exception e) {
            log.error(ERROR_WHILE_FETCHING_FROM_CASE, e);
            throw new CustomException(ERROR_WHILE_FETCHING_FROM_CASE, e.getMessage());
        }
    }

    public List<CourtCase> getCaseDetailsForSingleTonCriteria(CaseSearchRequest caseSearchRequest) {
        Object courtCase = cacheUtil.findById(
                caseSearchRequest.getCriteria().get(0).getTenantId() + ":"
                        + caseSearchRequest.getCriteria().get(0).getFilingNumber());
        if (courtCase != null) {
            return List.of(objectMapper.convertValue(courtCase, CourtCase.class));
        }
        CaseListResponse caseListResponse = searchCaseDetails(caseSearchRequest);
        cacheUtil.save(caseListResponse.getCriteria().get(0).getTenantId() + ":"
                        + caseListResponse.getCriteria().get(0).getFilingNumber(),
                caseListResponse.getCriteria().get(0).getResponseList().get(0));
        return caseListResponse.getCriteria().get(0).getResponseList();
    }


    public CaseResponse updateCase(CaseRequest request) {
        try {
            org.pucar.dristi.caselifecycle.cases.internal.web.models.CaseRequest bridgedRequest =
                    objectMapper.convertValue(request,
                            org.pucar.dristi.caselifecycle.cases.internal.web.models.CaseRequest.class);
            org.pucar.dristi.caselifecycle.cases.internal.web.models.CourtCase apiResult =
                    caseApi.update(bridgedRequest);
            if (apiResult != null) {
                CourtCase courtCase = objectMapper.convertValue(apiResult, CourtCase.class);
                cacheUtil.save(courtCase.getTenantId() + ":" + courtCase.getFilingNumber(), courtCase);
                return CaseResponse.builder().cases(List.of(courtCase)).build();
            }
            return CaseResponse.builder().build();
        } catch (Exception e) {
            log.error(SEARCHER_SERVICE_EXCEPTION, e);
            throw new CustomException();
        }
    }

    public List<Party> getRespondentOrComplainant(CourtCase caseDetails, String type) {
        return caseDetails.getLitigants()
                .stream()
                .filter(item -> item.getPartyType().contains(type))
                .collect(Collectors.toList());
    }


    public CaseResponse processProfileRequest(ProcessProfileRequest request) {
        try {
            org.pucar.dristi.caselifecycle.cases.internal.web.models.ProcessProfileRequest bridgedRequest =
                    objectMapper.convertValue(request,
                            org.pucar.dristi.caselifecycle.cases.internal.web.models.ProcessProfileRequest.class);
            org.pucar.dristi.caselifecycle.cases.internal.web.models.CourtCase apiResult =
                    caseApi.processProfile(bridgedRequest);
            if (apiResult != null) {
                CourtCase courtCase = objectMapper.convertValue(apiResult, CourtCase.class);
                return CaseResponse.builder().cases(List.of(courtCase)).build();
            }
            return CaseResponse.builder().build();
        } catch (Exception e) {
            log.error(SEARCHER_SERVICE_EXCEPTION, e);
            throw new CustomException();
        }
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

    public void addWitnessToCase(WitnessDetailsRequest witnessDetailsRequest) {
        try {
            org.pucar.dristi.caselifecycle.cases.internal.web.models.v2.WitnessDetailsRequest bridgedRequest =
                    objectMapper.convertValue(witnessDetailsRequest,
                            org.pucar.dristi.caselifecycle.cases.internal.web.models.v2.WitnessDetailsRequest.class);
            caseApi.addWitnessToCase(bridgedRequest);
        } catch (Exception e) {
            log.error(ERROR_WHILE_FETCHING_FROM_CASE, e);
            throw new CustomException(ERROR_WHILE_FETCHING_FROM_CASE, e.getMessage());
        }
    }

    public void updateLprDetailsInCase(CaseRequest caseRequest) {
        try {
            org.pucar.dristi.caselifecycle.cases.internal.web.models.CaseRequest bridgedRequest =
                    objectMapper.convertValue(caseRequest,
                            org.pucar.dristi.caselifecycle.cases.internal.web.models.CaseRequest.class);
            caseApi.updateLPRDetails(bridgedRequest);
        } catch (Exception e) {
            log.error(ERROR_WHILE_FETCHING_FROM_CASE, e);
            throw new CustomException(ERROR_WHILE_FETCHING_FROM_CASE, e.getMessage());
        }
    }
}
