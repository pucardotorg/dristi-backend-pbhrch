// HAND-CURATED — do not regenerate
package org.pucar.dristi.common.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.pucar.dristi.common.config.CommonConfiguration;
import org.pucar.dristi.common.models.individual.IndividualSearch;
import org.pucar.dristi.common.models.individual.IndividualSearchRequest;
import org.pucar.dristi.common.repository.ServiceRequestRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.pucar.dristi.common.config.CommonConstants.INDIVIDUAL_SERVICE_EXCEPTION;
import static org.pucar.dristi.common.config.CommonConstants.INDIVIDUAL_UTILITY_EXCEPTION;

/**
 * Calls the Individual service.
 *
 * <p>Three call shapes are supported:
 * <ul>
 *   <li>{@link #getIndividualByIndividualId} — generic over the caller's
 *       Individual type. Returns the deserialised list under the JSON
 *       {@code "Individual"} array.</li>
 *   <li>{@link #getIndividualId(RequestInfo)} — convenience that builds a
 *       default {@link IndividualSearchRequest} from the RequestInfo's
 *       user UUID and tenantId, then returns the first matching
 *       individualId. Used by services that just need "who is making
 *       this request".</li>
 *   <li>{@link #getIndividual} / {@link #getIndividualId(IndividualSearchRequest, StringBuilder)}
 *       — lower-level helpers exposing the raw {@link JsonNode} for
 *       callers that don't have a typed Individual model.</li>
 * </ul>
 */
@Component("commonIndividualUtil")
public class IndividualUtil {

    private static final Logger log = LoggerFactory.getLogger(IndividualUtil.class);
    private static final String INDIVIDUAL_NODE = "Individual";
    private static final String INDIVIDUAL_ID_FIELD = "individualId";

    private final ServiceRequestRepository serviceRequestRepository;
    private final ObjectMapper objectMapper;
    private final CommonConfiguration configs;

    @Autowired
    public IndividualUtil(
            ServiceRequestRepository serviceRequestRepository,
            ObjectMapper objectMapper,
            CommonConfiguration configs) {
        this.serviceRequestRepository = serviceRequestRepository;
        this.objectMapper = objectMapper;
        this.configs = configs;
    }

    /**
     * Generic typed search.
     *
     * @param individualRequest the IndividualSearchRequest payload
     * @param uri                full target URI
     * @param individualType     the Individual class the caller wants back
     */
    public <T> List<T> getIndividualByIndividualId(
            Object individualRequest, StringBuilder uri, Class<T> individualType) {
        List<T> individuals = new ArrayList<>();
        try {
            JsonNode individualNode = fetchIndividualArray(individualRequest, uri);
            if (individualNode.isArray()) {
                for (JsonNode node : individualNode) {
                    individuals.add(objectMapper.treeToValue(node, individualType));
                }
            }
        } catch (Exception e) {
            log.error("Error occurred in individual utility", e);
            throw new CustomException(
                    INDIVIDUAL_UTILITY_EXCEPTION,
                    "Error in individual utility service: " + e.getMessage());
        }
        return individuals;
    }

    /**
     * "Who is making this request?" — builds a default
     * IndividualSearchRequest from {@code requestInfo.userInfo.uuid} and
     * returns the first matching {@code individualId}, or empty string if
     * none found.
     */
    public String getIndividualId(RequestInfo requestInfo) {
        try {
            IndividualSearchRequest request = new IndividualSearchRequest();
            request.setRequestInfo(requestInfo);
            IndividualSearch search = new IndividualSearch();
            search.setUserUuid(Collections.singletonList(requestInfo.getUserInfo().getUuid()));
            request.setIndividual(search);

            StringBuilder uri = new StringBuilder(configs.getIndividualHost())
                    .append(configs.getIndividualSearchEndpoint())
                    .append("?limit=1")
                    .append("&offset=0")
                    .append("&tenantId=").append(requestInfo.getUserInfo().getTenantId())
                    .append("&includeDeleted=true");
            return getIndividualId(request, uri);
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error in search individual service :: {}", e.toString());
            throw new CustomException(
                    INDIVIDUAL_SERVICE_EXCEPTION,
                    "Error in search individual service: " + e.getMessage());
        }
    }

    /**
     * Returns the {@code individualId} of the first match, or empty string.
     *
     * <p>{@code individualRequest} is intentionally typed as {@link Object}
     * because services have their own {@code IndividualSearchRequest}
     * variants with extra fields. The request is sent verbatim — the
     * canonical only inspects the JSON response.
     */
    public String getIndividualId(Object individualRequest, StringBuilder uri) {
        JsonNode individual = getIndividual(individualRequest, uri);
        if (!ObjectUtils.isEmpty(individual) && individual.hasNonNull(INDIVIDUAL_ID_FIELD)) {
            return individual.get(INDIVIDUAL_ID_FIELD).asText();
        }
        return "";
    }

    /**
     * "Does any individual match this search?" — true iff the response array
     * is non-empty and the first element has an {@code individualId}.
     */
    public Boolean individualCall(Object individualRequest, StringBuilder uri) {
        try {
            JsonNode individualNode = fetchIndividualArray(individualRequest, uri);
            return individualNode.isArray()
                    && !individualNode.isEmpty()
                    && individualNode.get(0).hasNonNull(INDIVIDUAL_ID_FIELD);
        } catch (CustomException e) {
            log.error("Custom Exception occurred in individual Utility :: {}", e.toString());
            throw e;
        } catch (Exception e) {
            throw new CustomException(
                    INDIVIDUAL_UTILITY_EXCEPTION,
                    "Exception in individual utility service: " + e.getMessage());
        }
    }

    /** Convenience: {@code !getIndividualId(...).isEmpty()}. */
    public Boolean individualExists(Object individualRequest, StringBuilder uri) {
        return !getIndividualId(individualRequest, uri).isEmpty();
    }

    /**
     * Returns the first object under the {@code "Individual"} array as a
     * raw {@link JsonNode}. Empty {@link JsonNode} when no match.
     */
    public JsonNode getIndividual(Object individualRequest, StringBuilder uri) {
        try {
            JsonNode individualNode = fetchIndividualArray(individualRequest, uri);
            if (individualNode.isArray() && !individualNode.isEmpty() && individualNode.get(0) != null) {
                return individualNode.get(0);
            }
            return objectMapper.createObjectNode();
        } catch (CustomException e) {
            log.error("Custom Exception occurred in individual Utility :: {}", e.toString());
            throw e;
        } catch (Exception e) {
            throw new CustomException(
                    INDIVIDUAL_UTILITY_EXCEPTION,
                    "Exception in individual utility service: " + e.getMessage());
        }
    }

    private JsonNode fetchIndividualArray(Object individualRequest, StringBuilder uri) throws Exception {
        Object responseMap = serviceRequestRepository.fetchResult(uri, individualRequest);
        if (responseMap == null) {
            return objectMapper.createArrayNode();
        }
        String jsonString = objectMapper.writeValueAsString(responseMap);
        log.debug("IndividualUtil response :: {}", jsonString);
        JsonNode rootNode = objectMapper.readTree(jsonString);
        return rootNode.path(INDIVIDUAL_NODE);
    }
}
