package org.pucar.dristi.caselifecycle.ordermanagement.internal.util;

import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.pucar.dristi.caselifecycle.ordermanagement.internal.web.models.courtCase.CourtCase;
import org.pucar.dristi.caselifecycle.ordermanagement.internal.web.models.courtCase.Party;
import org.pucar.dristi.common.contract.advocate.Advocate;
import org.pucar.dristi.identityaccess.advocate.AdvocateApi;

import java.util.*;
import java.util.stream.Collectors;

@Component("ordermanagementAdvocateUtil")
@Slf4j
public class AdvocateUtil {

    private final AdvocateApi advocateApi;
    private final JsonUtil jsonUtil;

    @Autowired
    public AdvocateUtil(AdvocateApi advocateApi, JsonUtil jsonUtil) {
        this.advocateApi = advocateApi;
        this.jsonUtil = jsonUtil;
    }

    /**
     * Builds an advocate {@code individualId -> username} map for the
     * supplied advocate ids via {@link AdvocateApi#searchAdvocatesById}.
     * One direct call per id (in-process); active-filter applied per
     * the legacy REST behaviour.
     */
    public Map<String, String> getAdvocate(RequestInfo requestInfo, List<String> advocateIds) {
        Map<String, String> map = new HashMap<>();
        for (String id : advocateIds) {
            List<Advocate> advocates = advocateApi.searchAdvocatesById(requestInfo, id).stream()
                    .filter(Advocate::getIsActive)
                    .toList();
            for (Advocate advocate : advocates) {
                map.put(advocate.getIndividualId(), getUserName(advocate));
            }
        }
        return map;
    }

    private String getUserName(Advocate advocate) {
        @SuppressWarnings("unchecked")
        Map<String, Object> additionalDetails = (Map<String, Object>) advocate.getAdditionalDetails();
        return additionalDetails != null ? (String) additionalDetails.get("username") : null;
    }

    public Map<String, List<String>> getLitigantAdvocateMapping(CourtCase caseDetails) {
        Map<String, List<String>> litigants = new HashMap<>();

        if (caseDetails == null || caseDetails.getLitigants() == null) {
            return litigants;
        }

        for (Party litigant : caseDetails.getLitigants()) {
            List<String> list = Optional.ofNullable(caseDetails.getRepresentatives())
                    .orElse(Collections.emptyList())
                    .stream()
                    .filter(rep -> rep.getRepresenting() != null && rep.getRepresenting()
                            .stream()
                            .anyMatch(lit -> lit.getIndividualId().equals(litigant.getIndividualId()))
                            && getUUIDFromAdditionalDetails(rep.getAdditionalDetails()) != null)
                    .map(rep -> getUUIDFromAdditionalDetails(rep.getAdditionalDetails()))
                    .collect(Collectors.toList());

            String litigantUuid = getUUIDFromAdditionalDetails(litigant.getAdditionalDetails());
            if (!list.isEmpty()) {
                litigants.put(litigantUuid, list);
            } else {
                litigants.put(litigantUuid, Collections.singletonList(litigantUuid));
            }
        }
        return litigants;
    }

    private String getUUIDFromAdditionalDetails(Object additionalDetails) {
        return jsonUtil.getNestedValue(additionalDetails, List.of("uuid"), String.class);
    }
}
