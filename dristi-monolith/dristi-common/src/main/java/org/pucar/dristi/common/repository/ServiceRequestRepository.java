// HAND-CURATED — imports rewired to dristi-common; do not regenerate
// AUTO-EXTRACTED INTO dristi-common BY scripts/migration/dristi_common/03_build_canonical.py
// Source: dristi-services/hearing/src/main/java/org/pucar/dristi/repository/ServiceRequestRepository.java
// NOTE: imports referencing service-internal classes (ServiceConstants,
// Configuration, web.models.*) may need follow-up — see Phase 4.
package org.pucar.dristi.common.repository;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.slf4j.Slf4j;
import org.egov.tracer.model.ServiceCallException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.pucar.dristi.common.config.CommonConstants.*;

@Repository("commonServiceRequestRepository")
@Slf4j
public class ServiceRequestRepository {

    private ObjectMapper mapper;

    private RestTemplate restTemplate;


    @Autowired
    public ServiceRequestRepository(ObjectMapper mapper, RestTemplate restTemplate) {
        this.mapper = mapper;
        this.restTemplate = restTemplate;
    }

    public Object fetchResult(StringBuilder uri, Object request) {
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        Object response = null;
        try {
            response = restTemplate.postForObject(uri.toString(), request, Map.class);
        }catch(HttpClientErrorException e) {
            log.error(EXTERNAL_SERVICE_EXCEPTION,e);
            throw new ServiceCallException(e.getResponseBodyAsString());
        }catch(Exception e) {
            log.error(SEARCHER_SERVICE_EXCEPTION,e);
        }

        return response;
    }

    /**
     * POST that expects a Boolean response body — used by services calling
     * existence/check endpoints (e.g. case validation).
     */
    public Boolean getResult(StringBuilder uri, Object request) {
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        Boolean response = null;
        try {
            response = restTemplate.postForObject(uri.toString(), request, Boolean.class);
        } catch (HttpClientErrorException e) {
            log.error(EXTERNAL_SERVICE_EXCEPTION + " URI: {}", uri, e);
            throw new ServiceCallException(e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error(SEARCHER_SERVICE_EXCEPTION, e);
        }
        return response;
    }
}