package org.pucar.dristi.caselifecycle.order.internal.util;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import org.egov.tracer.model.CustomException;
import org.pucar.dristi.caselifecycle.order.internal.config.Configuration;
import org.pucar.dristi.common.repository.ServiceRequestRepository;
import org.pucar.dristi.caselifecycle.order.internal.web.models.HearingListResponse;
import org.pucar.dristi.caselifecycle.order.internal.web.models.HearingSearchRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.pucar.dristi.caselifecycle.order.internal.config.ServiceConstants.ERROR_WHILE_FETCHING_FROM_HEARING_SERVICE;


@Component("orderHearingUtil")
@Slf4j
public class HearingUtil {

    private final Configuration
            configuration;

    private final ServiceRequestRepository serviceRequestRepository;

    private final ObjectMapper mapper;

    @Autowired
    public HearingUtil(Configuration configuration, ServiceRequestRepository serviceRequestRepository, ObjectMapper mapper) {
        this.configuration = configuration;
        this.serviceRequestRepository = serviceRequestRepository;
        this.mapper = mapper;
    }

    public HearingListResponse getHearings(HearingSearchRequest hearingSearchRequest) {

        StringBuilder uri = new StringBuilder();
        uri.append(configuration.getHearingHost()).append(configuration.getHearingSearchPath());
        Object response;
        HearingListResponse hearingListResponse;
        try {
            response = serviceRequestRepository.fetchResult(uri, hearingSearchRequest);
            hearingListResponse = mapper.convertValue(response, HearingListResponse.class);
        }catch(Exception e) {
            log.error(ERROR_WHILE_FETCHING_FROM_HEARING_SERVICE, e);
            throw new CustomException(ERROR_WHILE_FETCHING_FROM_HEARING_SERVICE, e.getMessage());
        }
        return hearingListResponse;

    }

}
