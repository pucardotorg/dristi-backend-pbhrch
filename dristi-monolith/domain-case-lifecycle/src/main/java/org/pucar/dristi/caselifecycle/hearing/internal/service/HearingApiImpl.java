package org.pucar.dristi.caselifecycle.hearing.internal.service;

import lombok.RequiredArgsConstructor;
import org.pucar.dristi.common.hearing.HearingApi;
import org.pucar.dristi.common.contract.hearing.Hearing;
import org.pucar.dristi.common.contract.hearing.HearingRequest;
import org.pucar.dristi.common.contract.hearing.HearingSearchRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("hearingApiImpl")
@RequiredArgsConstructor
public class HearingApiImpl implements HearingApi {

    private final HearingService hearingService;

    @Override
    public List<Hearing> search(HearingSearchRequest request) {
        return hearingService.searchHearing(request);
    }

    @Override
    public void update(HearingRequest request) {
        hearingService.updateTranscriptAdditionalAttendees(request);
    }
}
