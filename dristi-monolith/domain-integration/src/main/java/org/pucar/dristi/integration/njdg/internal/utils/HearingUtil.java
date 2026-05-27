package org.pucar.dristi.integration.njdg.internal.utils;

import org.pucar.dristi.integration.njdg.internal.config.TransformerProperties;
import org.pucar.dristi.integration.njdg.internal.model.hearing.Hearing;
import org.pucar.dristi.integration.njdg.internal.model.hearing.HearingListResponse;
import org.pucar.dristi.integration.njdg.internal.model.hearing.HearingSearchRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.tracer.model.CustomException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class HearingUtil {

    private final RestTemplate restTemplate;
    private final ObjectMapper mapper;
    private final TransformerProperties properties;

    public HearingUtil(RestTemplate restTemplate, ObjectMapper mapper, TransformerProperties properties) {
        this.restTemplate = restTemplate;
        this.mapper = mapper;
        this.properties = properties;
    }

    public List<Hearing> fetchHearingDetails(HearingSearchRequest hearingSearchRequest) {
        StringBuilder uri = new StringBuilder();
        uri.append(properties.getHearingHost()).append(properties.getHearingSearchPath());

        Object response;
        HearingListResponse hearingListResponse;
        try {
            response = restTemplate.postForObject(uri.toString(), hearingSearchRequest, Map.class);
            hearingListResponse = mapper.convertValue(response, HearingListResponse.class);
        } catch (Exception e) {
            log.error("ERROR_WHILE_FETCHING_FROM_HEARING", e);
            throw new CustomException("ERROR_WHILE_FETCHING_FROM_HEARING", e.getMessage());
        }
        if(hearingListResponse==null){
            return new ArrayList<>();
        }
        return hearingListResponse.getHearingList();
    }
}
