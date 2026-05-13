package org.pucar.dristi.caselifecycle.task.internal.util;

import lombok.extern.slf4j.Slf4j;
import org.pucar.dristi.common.contract.task.CourtCase;
import org.pucar.dristi.common.contract.task.Party;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@Slf4j
public class AdvocateUtil {

    private final JsonUtil jsonUtil;

    @Autowired
    public AdvocateUtil(JsonUtil jsonUtil) {
        this.jsonUtil = jsonUtil;
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
