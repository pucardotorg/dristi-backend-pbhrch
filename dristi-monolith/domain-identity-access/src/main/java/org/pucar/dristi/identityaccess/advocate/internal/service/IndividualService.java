package org.pucar.dristi.identityaccess.advocate.internal.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.models.individual.Individual;
import org.egov.tracer.model.CustomException;
import org.pucar.dristi.identityaccess.advocate.internal.config.Configuration;
import org.pucar.dristi.common.contract.advocate.IndividualSearch;
import org.pucar.dristi.common.contract.advocate.IndividualSearchRequest;
import org.pucar.dristi.common.util.IndividualUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.pucar.dristi.identityaccess.advocate.internal.config.ServiceConstants.INDIVIDUAL_SERVICE_EXCEPTION;

@Service
@Slf4j
public class IndividualService {
    private final IndividualUtil individualUtils;
    private final Configuration config;

    @Autowired
    public IndividualService(@Qualifier("commonIndividualUtil") IndividualUtil individualUtils, Configuration config) {
        this.individualUtils = individualUtils;
        this.config = config;
    }

    public Boolean searchIndividual(RequestInfo requestInfo, String individualId, Map<String, String> individualUserUUID) {
        try {
            IndividualSearchRequest individualSearchRequest = new IndividualSearchRequest();
            individualSearchRequest.setRequestInfo(requestInfo);
            IndividualSearch individualSearch = new IndividualSearch();
            log.info("Individual Id :: {}", individualId);
            individualSearch.setIndividualId(individualId);
            individualSearchRequest.setIndividual(individualSearch);
            StringBuilder uri = new StringBuilder(config.getIndividualHost()).append(config.getIndividualSearchEndpoint());
            uri.append("?limit=1000").append("&offset=0").append("&tenantId=").append(requestInfo.getUserInfo().getTenantId());

            JsonNode node = individualUtils.getIndividual(individualSearchRequest, uri);
            boolean found = !node.isEmpty() && node.hasNonNull("individualId");
            if (found && node.hasNonNull("userUuid")) {
                individualUserUUID.put("userUuid", node.get("userUuid").asText());
            }
            return found;

        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error in search individual service :: {}", e.toString());
            throw new CustomException(INDIVIDUAL_SERVICE_EXCEPTION, "Error in search individual service" + e.getMessage());
        }
    }

    public List<Individual> getIndividualsByIndividualId(RequestInfo requestInfo, String individualId) throws CustomException {
        try {
            IndividualSearchRequest individualSearchRequest = new IndividualSearchRequest();
            individualSearchRequest.setRequestInfo(requestInfo);
            IndividualSearch individualSearch = new IndividualSearch();
            individualSearch.setIndividualId(individualId);
            individualSearchRequest.setIndividual(individualSearch);
            StringBuilder uri = buildIndividualSearchUri(requestInfo, Collections.singletonList(individualId));
            List<Individual> individuals = individualUtils.getIndividualByIndividualId(
                    individualSearchRequest, uri, Individual.class);
            return individuals != null ? individuals : Collections.emptyList();
        } catch (Exception e) {
            log.error("Error in search individual service: ", e);
            return Collections.emptyList();
        }
    }

    private StringBuilder buildIndividualSearchUri(RequestInfo requestInfo, List<String> individualIds) {
        return new StringBuilder(config.getIndividualHost())
                .append(config.getIndividualSearchEndpoint())
                .append("?limit=").append(individualIds.size())
                .append("&offset=0")
                .append("&tenantId=").append(requestInfo.getUserInfo().getTenantId());
    }
}
