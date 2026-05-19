package org.pucar.dristi.caselifecycle.scheduler.internal.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.pucar.dristi.caselifecycle.scheduler.internal.config.Configuration;
import org.pucar.dristi.caselifecycle.scheduler.internal.repository.SchedulerServiceRequestHelper;
import org.pucar.dristi.caselifecycle.scheduler.internal.web.models.ApplicationRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("schedulerApplicationUtil")
@Slf4j
public class ApplicationUtil {

    private final Configuration config;
    private final SchedulerServiceRequestHelper requestRepository;
    private final ObjectMapper mapper;

    @Autowired
    public ApplicationUtil(Configuration config, SchedulerServiceRequestHelper requestRepository, ObjectMapper mapper) {
        this.config = config;
        this.requestRepository = requestRepository;
        this.mapper = mapper;
    }

    public JsonNode getApplications(ApplicationRequest applicationRequest){
        log.info("ApplicationUtil getApplications");

        StringBuilder url = new StringBuilder();
        url.append(config.getApplicationHost()).append(config.getApplicationSearchEndpoint());

        Object response = requestRepository.postMethod(url, applicationRequest);
        JsonNode applicationList = null;
        try {
            JsonNode responseNode = mapper.readTree(response.toString());
            applicationList = responseNode.get("applicationList");
            log.info("ApplicationUtil getApplications response: {}", response);
        } catch (Exception e) {
            log.error("ApplicationUtil getApplications error: {}", e.getMessage());
        }

        return applicationList;
    }
}
