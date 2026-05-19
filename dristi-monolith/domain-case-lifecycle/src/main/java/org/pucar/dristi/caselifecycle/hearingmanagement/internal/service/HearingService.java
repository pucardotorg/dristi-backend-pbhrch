package org.pucar.dristi.caselifecycle.hearingmanagement.internal.service;

import org.pucar.dristi.caselifecycle.hearing.HearingApi;
import org.pucar.dristi.caselifecycle.hearingmanagement.internal.enrichment.HearingsEnrichment;
import org.pucar.dristi.common.contract.hearing.Hearing;
import org.pucar.dristi.common.contract.hearingmanagement.HearingCriteria;
import org.pucar.dristi.common.contract.hearingmanagement.HearingSearchListResponse;
import org.pucar.dristi.common.contract.hearingmanagement.HearingSearchRequest;
import org.pucar.dristi.common.contract.hearingmanagement.HearingSearchResponse;
import lombok.extern.slf4j.Slf4j;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static org.pucar.dristi.caselifecycle.hearingmanagement.internal.config.ServiceConstants.HEARING_SEARCH_EXCEPTION;

@Service("hearingmanagementHearingService")
@Slf4j
public class HearingService {

    private final HearingApi hearingApi;

    private final HearingsEnrichment hearingsEnrichment;

    @Autowired
    public HearingService(HearingApi hearingApi, HearingsEnrichment hearingsEnrichment) {
        this.hearingApi = hearingApi;
        this.hearingsEnrichment = hearingsEnrichment;
    }

    public HearingSearchListResponse searchHearings(HearingSearchRequest hearingSearchRequest) {

        log.info("search hearings, result= IN_PROGRESS,  request = {} ", hearingSearchRequest);

        try {
            List<Hearing> hearings = hearingApi.search(toApiRequest(hearingSearchRequest));

            List<HearingSearchResponse> hearingSearchResponseList = new ArrayList<>();

            if (hearings != null && !hearings.isEmpty()) {
                hearingSearchResponseList = hearingsEnrichment.enrichHearings(hearings);
                return HearingSearchListResponse.builder()
                        .totalCount(hearings.size())
                        .hearingList(hearingSearchResponseList)
                        .build();
            }

            log.info("search hearings, result= SUCCESS, response = {} ", hearingSearchResponseList);

            return HearingSearchListResponse.builder()
                    .totalCount(0)
                    .hearingList(hearingSearchResponseList)
                    .build();

        } catch (Exception e) {
            log.error("Error occurred while searching hearings");
            throw new CustomException(HEARING_SEARCH_EXCEPTION, e.getMessage());
        }
    }

    private org.pucar.dristi.common.contract.hearing.HearingSearchRequest toApiRequest(HearingSearchRequest req) {
        if (req == null) return null;
        HearingCriteria c = req.getCriteria();
        return org.pucar.dristi.common.contract.hearing.HearingSearchRequest.builder()
                .requestInfo(req.getRequestInfo())
                .criteria(c == null ? null :
                        org.pucar.dristi.common.contract.hearing.HearingCriteria.builder()
                                .hearingId(c.getHearingId())
                                .hearingType(c.getHearingType())
                                .cnrNumber(c.getCnrNumber())
                                .filingNumber(c.getFilingNumber())
                                .tenantId(c.getTenantId())
                                .applicationNumber(c.getApplicationNumber())
                                .fromDate(c.getFromDate())
                                .toDate(c.getToDate())
                                .attendeeIndividualId(c.getAttendeeIndividualId())
                                .courtId(c.getCourtId())
                                .build())
                .pagination(req.getPagination() == null ? null :
                        org.pucar.dristi.common.contract.hearing.Pagination.builder()
                                .limit(req.getPagination().getLimit())
                                .offSet(req.getPagination().getOffSet())
                                .build())
                .build();
    }
}
