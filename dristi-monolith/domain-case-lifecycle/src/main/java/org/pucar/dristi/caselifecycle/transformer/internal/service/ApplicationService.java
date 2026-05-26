package org.pucar.dristi.caselifecycle.transformer.internal.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.tracer.model.CustomException;
import org.pucar.dristi.caselifecycle.transformer.internal.config.ServiceConstants;
import org.pucar.dristi.caselifecycle.transformer.internal.config.TransformerProperties;
import org.pucar.dristi.caselifecycle.transformer.internal.models.Application;
import org.pucar.dristi.caselifecycle.transformer.internal.models.ApplicationData;
import org.pucar.dristi.caselifecycle.transformer.internal.models.ApplicationRequest;
import org.pucar.dristi.caselifecycle.transformer.internal.models.Order;
import org.pucar.dristi.caselifecycle.transformer.internal.producer.TransformerProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.LinkedHashMap;

@Slf4j
@Service("transformerApplicationService")
public class ApplicationService {
    private static final Logger logger = LoggerFactory.getLogger(ApplicationService.class);


    private final ElasticSearchService elasticSearchService;
    private final TransformerProperties properties;
    private final TransformerProducer producer;
    private final ObjectMapper objectMapper;


    @Autowired
    public ApplicationService(ElasticSearchService elasticSearchService, TransformerProperties properties, TransformerProducer producer, ObjectMapper objectMapper) {
        this.elasticSearchService = elasticSearchService;
        this.properties = properties;
        this.producer = producer;
        this.objectMapper = objectMapper;


    }

    private ApplicationData fetchApplication(String fieldValue) throws IOException {
        LinkedHashMap<String, Object> sourceMap = elasticSearchService.getDocumentByField(ServiceConstants.APPLICATION_INDEX, ServiceConstants.APPLICATION_NUMBER, fieldValue);
        if (null == sourceMap || null == sourceMap.get("Data")) {
            log.error("No application data found for {}", fieldValue);
            throw new CustomException("APPLICATION_SEARCH_EMPTY", ServiceConstants.APPLICATION_SEARCH_EMPTY);
        }

        return objectMapper.convertValue(sourceMap.get("Data"), ApplicationData.class);

    }

    public void updateApplication(Order order, String applicationNumber) {

        try {

            ApplicationData applicationData = fetchApplication(applicationNumber);
            Application application = applicationData.getApplicationDetails();

            application.setOrderDetails(order);
            application.setAuditDetails(applicationData.getAuditDetails());

            ApplicationRequest applicationRequest = new ApplicationRequest();
            applicationRequest.setApplication(application);

            producer.push(properties.getUpdateApplicationOrderTopic(), applicationRequest);

        } catch (Exception e) {
            log.error("error executing application search query", e);
            throw new CustomException("ERROR_APPLICATION_SEARCH", ServiceConstants.ERROR_APPLICATION_SEARCH);
        }
    }


}
