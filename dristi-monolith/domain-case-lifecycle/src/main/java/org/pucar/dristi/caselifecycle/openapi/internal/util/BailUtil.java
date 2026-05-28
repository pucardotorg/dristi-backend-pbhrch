package org.pucar.dristi.caselifecycle.openapi.internal.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.User;
import org.egov.tracer.model.CustomException;
import org.pucar.dristi.caselifecycle.bailbond.BailbondApi;
import org.pucar.dristi.caselifecycle.openapi.internal.config.Configuration;
import org.pucar.dristi.caselifecycle.openapi.internal.web.models.bailbond.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.pucar.dristi.caselifecycle.openapi.internal.config.ServiceConstants.ERROR_WHILE_FETCHING_FROM_BAIL;

@Slf4j
@Component
public class BailUtil {

    private final RestTemplate restTemplate;
    private final ObjectMapper mapper;
    private final Configuration configs;
    private final BailbondApi bailbondApi;

    @Autowired
    public BailUtil(RestTemplate restTemplate, ObjectMapper mapper, Configuration configs, BailbondApi bailbondApi) {
        this.restTemplate = restTemplate;
        this.mapper = mapper;
        this.configs = configs;
        this.bailbondApi = bailbondApi;
    }

    public BailSearchResponse fetchBails(BailSearchCriteria bailCriteria, RequestInfo requestInfo) {
        try {
            org.pucar.dristi.common.contract.bailbond.BailSearchRequest bridged =
                    org.pucar.dristi.common.contract.bailbond.BailSearchRequest.builder()
                            .requestInfo(requestInfo)
                            .criteria(mapper.convertValue(bailCriteria,
                                    org.pucar.dristi.common.contract.bailbond.BailSearchCriteria.class))
                            .build();
            org.pucar.dristi.common.contract.bailbond.BailSearchResponse response = bailbondApi.searchBail(bridged);
            BailSearchResponse bailResponse = mapper.convertValue(response, BailSearchResponse.class);
            log.info("Bail response :: {}", bailResponse);
            return bailResponse;
        } catch (Exception e) {
            log.error(ERROR_WHILE_FETCHING_FROM_BAIL, e);
            throw new CustomException(ERROR_WHILE_FETCHING_FROM_BAIL, e.getMessage());
        }
    }

    public BailResponse updateBailBond(BailRequest bailRequest) {

        StringBuilder uri = new StringBuilder();
        uri.append(configs.getBailServiceHost()).append(configs.getBailServiceUpdateEndpoint());

        Object response;
        try {
            response = restTemplate.postForObject(uri.toString(), bailRequest, Map.class);
            return mapper.convertValue(response, BailResponse.class);
        } catch (Exception e) {
            log.error(ERROR_WHILE_FETCHING_FROM_BAIL, e);
            throw new CustomException(ERROR_WHILE_FETCHING_FROM_BAIL, e.getMessage());
        }

    }

}
