// HAND-CURATED — do not regenerate
package org.pucar.dristi.common.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.egov.tracer.model.CustomException;
import org.pucar.dristi.common.repository.ServiceRequestRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static org.pucar.dristi.common.config.CommonConstants.INDIVIDUAL_UTILITY_EXCEPTION;

/**
 * Calls the Individual service and unwraps the array under the
 * <code>"Individual"</code> JSON node, deserializing each element to the
 * caller-supplied type.
 *
 * <p>Made generic so dristi-common does not depend on any specific
 * Individual model (services keep their own variant of
 * {@code org.pucar.dristi.web.models.Individual}).
 */
@Component
public class IndividualUtil {

    private static final Logger log = LoggerFactory.getLogger(IndividualUtil.class);

    private final ServiceRequestRepository serviceRequestRepository;
    private final ObjectMapper objectMapper;

    @Autowired
    public IndividualUtil(ServiceRequestRepository serviceRequestRepository, ObjectMapper objectMapper) {
        this.serviceRequestRepository = serviceRequestRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * @param individualRequest service-specific IndividualSearchRequest payload
     * @param uri                full target URI
     * @param individualType     the Individual class the caller wants back
     * @param <T>                concrete Individual type
     */
    public <T> List<T> getIndividualByIndividualId(
            Object individualRequest, StringBuilder uri, Class<T> individualType) {
        List<T> individuals = new ArrayList<>();
        try {
            Object responseMap = serviceRequestRepository.fetchResult(uri, individualRequest);
            if (responseMap == null) {
                return individuals;
            }
            String jsonString = objectMapper.writeValueAsString(responseMap);
            log.debug("IndividualUtil response :: {}", jsonString);
            JsonNode rootNode = objectMapper.readTree(jsonString);
            JsonNode individualNode = rootNode.path("Individual");
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
}
