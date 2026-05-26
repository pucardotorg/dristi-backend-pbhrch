package org.pucar.dristi.caselifecycle.transformer.internal.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.tracer.model.CustomException;
import org.pucar.dristi.caselifecycle.hearing.HearingApi;
import org.pucar.dristi.caselifecycle.transformer.internal.models.Hearing;
import org.pucar.dristi.caselifecycle.transformer.internal.models.HearingSearchRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;


@Component("transformerHearingUtil")
@Slf4j
public class HearingUtil {

    private final HearingApi hearingApi;
    private final ObjectMapper mapper;

    @Autowired
    public HearingUtil(HearingApi hearingApi, ObjectMapper mapper) {
        this.hearingApi = hearingApi;
        this.mapper = mapper;
    }

    public List<Hearing> fetchHearingDetails(HearingSearchRequest hearingSearchRequest) {
        if (hearingSearchRequest == null) {
            throw new IllegalArgumentException("HearingSearchRequest cannot be null");
        }

        try {
            org.pucar.dristi.common.contract.hearing.HearingSearchRequest commonReq =
                    mapper.convertValue(hearingSearchRequest,
                            org.pucar.dristi.common.contract.hearing.HearingSearchRequest.class);
            List<org.pucar.dristi.common.contract.hearing.Hearing> commonHearings = hearingApi.search(commonReq);
            if (commonHearings == null) {
                return new ArrayList<>();
            }
            return mapper.convertValue(commonHearings, new TypeReference<List<Hearing>>() {});
        } catch (Exception e) {
            log.error("Unexpected error while fetching hearing details", e);
            throw new CustomException("UNEXPECTED_ERROR_FETCHING_HEARINGS", e.getMessage());
        }
    }

}
