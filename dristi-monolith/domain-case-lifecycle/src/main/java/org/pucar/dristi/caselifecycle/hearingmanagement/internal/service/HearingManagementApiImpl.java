package org.pucar.dristi.caselifecycle.hearingmanagement.internal.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.slf4j.Slf4j;
import org.egov.tracer.model.ServiceCallException;
import org.pucar.dristi.common.contract.hearingmanagement.InboxRequest;
import org.pucar.dristi.common.contract.hearingmanagement.InboxResponse;
import org.pucar.dristi.common.hearingmanagement.HearingManagementApi;
import org.pucar.dristi.common.repository.ServiceRequestRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import static org.pucar.dristi.caselifecycle.hearingmanagement.internal.config.HearingManagementConstants.EXTERNAL_SERVICE_EXCEPTION;
import static org.pucar.dristi.caselifecycle.hearingmanagement.internal.config.HearingManagementConstants.SEARCHER_SERVICE_EXCEPTION;

@Service
@Slf4j
public class HearingManagementApiImpl implements HearingManagementApi {

    private final ObjectMapper objectMapper;
    private final ServiceRequestRepository serviceRequestRepository;

    @Value("${egov.inbox.host}")
    private String inboxHost;

    @Value("${egov.inbox.search.endpoint}")
    private String inboxSearchEndPoint;

    public HearingManagementApiImpl(ObjectMapper objectMapper,
                                    ServiceRequestRepository serviceRequestRepository) {
        this.objectMapper = objectMapper;
        this.serviceRequestRepository = serviceRequestRepository;
    }

    @Override
    public InboxResponse search(InboxRequest request) {
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        StringBuilder uri = new StringBuilder(inboxHost).append(inboxSearchEndPoint);
        InboxResponse inboxResponse = new InboxResponse();
        try {
            Object response = serviceRequestRepository.fetchResult(uri, request);
            inboxResponse = objectMapper.convertValue(response, InboxResponse.class);
        } catch (HttpClientErrorException e) {
            log.error(EXTERNAL_SERVICE_EXCEPTION, e);
            throw new ServiceCallException(e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error(SEARCHER_SERVICE_EXCEPTION, e);
        }
        return inboxResponse;
    }
}
