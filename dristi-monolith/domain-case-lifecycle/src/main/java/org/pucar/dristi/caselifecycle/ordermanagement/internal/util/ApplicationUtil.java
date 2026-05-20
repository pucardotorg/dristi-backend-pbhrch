package org.pucar.dristi.caselifecycle.ordermanagement.internal.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.slf4j.Slf4j;
import org.egov.tracer.model.CustomException;
import org.egov.tracer.model.ServiceCallException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.pucar.dristi.caselifecycle.ordermanagement.internal.config.Configuration;
import org.pucar.dristi.common.repository.ServiceRequestRepository;
import org.pucar.dristi.caselifecycle.ordermanagement.internal.web.models.application.*;

import java.util.List;

import static org.pucar.dristi.caselifecycle.ordermanagement.internal.config.ServiceConstants.EXTERNAL_SERVICE_EXCEPTION;
import static org.pucar.dristi.caselifecycle.ordermanagement.internal.config.ServiceConstants.SEARCHER_SERVICE_EXCEPTION;

@Component("ordermanagementApplicationUtil")
@Slf4j
public class ApplicationUtil {

    private final ObjectMapper objectMapper;
    private final Configuration configuration;
    private final ServiceRequestRepository serviceRequestRepository;
    private final CacheUtil cacheUtil;

    @Autowired
    public ApplicationUtil(ObjectMapper objectMapper, Configuration configuration, ServiceRequestRepository serviceRequestRepository, CacheUtil cacheUtil) {
        this.objectMapper = objectMapper;
        this.configuration = configuration;
        this.serviceRequestRepository = serviceRequestRepository;
        this.cacheUtil = cacheUtil;
    }

    // return list of application
    public List<Application> searchApplications(ApplicationSearchRequest request) {
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        StringBuilder uri = new StringBuilder(configuration.getApplicationHost()).append(configuration.getApplicationSearchEndPoint());
        Object response = serviceRequestRepository.fetchResult(uri, request);

        try {
            Object redisResponse = cacheUtil.findById(request.getCriteria().getTenantId() + ":" + request.getCriteria().getApplicationNumber());
            if (redisResponse != null) {
                Application application = objectMapper.readValue(objectMapper.writeValueAsString(redisResponse), Application.class);
                return List.of(application);
            }
            JsonNode jsonNode = objectMapper.valueToTree(response);
            ApplicationListResponse applicationSearchResult = objectMapper.readValue(jsonNode.toString(), ApplicationListResponse.class);
            cacheUtil.save(applicationSearchResult.getApplicationList().get(0).getTenantId() + ":" + applicationSearchResult.getApplicationList().get(0).getApplicationNumber(), applicationSearchResult.getApplicationList().get(0));
            return applicationSearchResult.getApplicationList();

        } catch (HttpClientErrorException e) {
            log.error(EXTERNAL_SERVICE_EXCEPTION, e);
            throw new ServiceCallException(e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error(SEARCHER_SERVICE_EXCEPTION, e);
            throw new CustomException(SEARCHER_SERVICE_EXCEPTION, e.getMessage()); // add log and code
        }
    }

    public ApplicationResponse updateApplication(ApplicationRequest request) {

        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        StringBuilder uri = new StringBuilder(configuration.getApplicationHost()).append(configuration.getApplicationUpdateEndPoint());
        Object response = serviceRequestRepository.fetchResult(uri, request);
        try {
            JsonNode jsonNode = objectMapper.valueToTree(response);
            ApplicationResponse applicationResponse = objectMapper.readValue(jsonNode.toString(), ApplicationResponse.class);
            if (applicationResponse != null) {
                Application application = objectMapper.convertValue(applicationResponse.getApplication(), Application.class);
                cacheUtil.save(application.getTenantId() + ":" + application.getApplicationNumber(), application);
            }
            return applicationResponse;
        } catch (HttpClientErrorException e) {
            log.error(EXTERNAL_SERVICE_EXCEPTION, e);
            throw new ServiceCallException(e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error(SEARCHER_SERVICE_EXCEPTION, e);
            throw new CustomException(SEARCHER_SERVICE_EXCEPTION, e.getMessage()); // add log and code
        }

    }
}
