package org.pucar.dristi.caselifecycle.ctc.internal.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.pucar.dristi.caselifecycle.cases.CaseApi;
import org.pucar.dristi.caselifecycle.cases.internal.web.models.AdvocateMapping;
import org.pucar.dristi.caselifecycle.cases.internal.web.models.CaseCriteria;
import org.pucar.dristi.caselifecycle.cases.internal.web.models.CaseListResponse;
import org.pucar.dristi.caselifecycle.cases.internal.web.models.CaseSearchRequest;
import org.pucar.dristi.caselifecycle.cases.internal.web.models.CourtCase;
import org.pucar.dristi.caselifecycle.cases.internal.web.models.Party;
import org.pucar.dristi.caselifecycle.cases.internal.web.models.POAHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

import static org.pucar.dristi.caselifecycle.ctc.internal.config.ServiceConstants.ERROR_WHILE_FETCHING_FROM_CASE;

@Slf4j
@Component("ctcCaseUtil")
public class CaseUtil {

    private final ObjectMapper mapper;

    private final CaseApi caseApi;

    @Autowired
    public CaseUtil(ObjectMapper mapper, CaseApi caseApi) {
        this.mapper = mapper;
        this.caseApi = caseApi;
    }

    public CourtCase getCase(String filingNumber, String courtId, RequestInfo requestInfo) {
        CaseSearchRequest request = CaseSearchRequest.builder()
                .requestInfo(requestInfo)
                .criteria(Collections.singletonList(CaseCriteria.builder()
                        .filingNumber(filingNumber)
                        .courtId(courtId)
                        .defaultFields(false)
                        .build()))
                .build();
        try {
            CaseListResponse response = caseApi.search(request);
            return Optional.ofNullable(response)
                    .map(CaseListResponse::getCriteria)
                    .filter(list -> !list.isEmpty())
                    .map(list -> list.get(0).getResponseList())
                    .filter(list -> !list.isEmpty())
                    .map(list -> list.get(0))
                    .orElse(null);
        } catch (Exception e) {
            log.error("Error fetching case", e);
            throw new CustomException(ERROR_WHILE_FETCHING_FROM_CASE, e.getMessage());
        }
    }

    public Map<String, String> extractPoaHolderUuids(CourtCase courtCase) {

        Map<String, String> uuidNameMap = new HashMap<>();

        if (courtCase.getPoaHolders() == null || courtCase.getPoaHolders().isEmpty()) {
            log.info("poa holder is null or empty");
            return new HashMap<>();
        }

        for (POAHolder poaHolder : courtCase.getPoaHolders()) {
            if (poaHolder.getAdditionalDetails() == null)
                continue;

            try {
                JsonNode node = mapper.valueToTree(poaHolder.getAdditionalDetails());

                String uuid = node.path("uuid").asText(null);

                if (uuid != null && !uuid.isBlank()) {
                    uuidNameMap.put(uuid, poaHolder.getName());
                }

            } catch (Exception e) {
                log.error("Failed extracting POA UUID", e);
            }
        }

        return uuidNameMap;
    }

    public Map<String, String> extractComplainantUuids(CourtCase courtCase) {

        Map<String, String> uuidNameMap = new HashMap<>();

        List<Party> litigants = courtCase.getLitigants();

        if (litigants == null || litigants.isEmpty())
            return uuidNameMap;

        for (Party litigant : litigants) {
            if (litigant.getAdditionalDetails() == null)
                continue;

            if (litigant.getPartyType() == null || !litigant.getPartyType().toLowerCase().contains("complainant"))
                continue;

            try {
                JsonNode node = mapper.valueToTree(litigant.getAdditionalDetails());

                String uuid = node.path("uuid").asText(null);
                String fullName = node.path("fullName").asText(null);

                if (uuid != null && !uuid.isBlank() && fullName != null && !fullName.isBlank()) {
                    uuidNameMap.put(uuid, fullName);
                }

            } catch (Exception e) {
                log.error("Failed extracting complainant UUID", e);
            }
        }

        return uuidNameMap;
    }

    public Map<String, String> extractRespondentUuids(CourtCase courtCase) {

        Map<String, String> uuidNameMap = new HashMap<>();

        List<Party> litigants = courtCase.getLitigants();

        if (litigants == null || litigants.isEmpty())
            return uuidNameMap;

        for (Party litigant : litigants) {
            if (litigant.getAdditionalDetails() == null)
                continue;

            if (litigant.getPartyType() == null || !litigant.getPartyType().toLowerCase().contains("respondent"))
                continue;

            try {
                JsonNode node = mapper.valueToTree(litigant.getAdditionalDetails());

                String uuid = node.path("uuid").asText(null);
                String fullName = node.path("fullName").asText(null);

                if (uuid != null && !uuid.isBlank() && fullName != null && !fullName.isBlank()) {
                    uuidNameMap.put(uuid, fullName);
                }

            } catch (Exception e) {
                log.error("Failed extracting respondent UUID", e);
            }
        }

        return uuidNameMap;
    }
}
