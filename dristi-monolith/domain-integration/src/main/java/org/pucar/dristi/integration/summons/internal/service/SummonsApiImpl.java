package org.pucar.dristi.integration.summons.internal.service;

import lombok.RequiredArgsConstructor;
import org.egov.common.contract.request.RequestInfo;
import org.pucar.dristi.common.contract.summons.ChannelMessage;
import org.pucar.dristi.common.contract.summons.ChannelReport;
import org.pucar.dristi.common.contract.summons.UpdateSummonsRequest;
import org.pucar.dristi.integration.summons.SummonsApi;
import org.springframework.stereotype.Service;

@Service("summonsApiImpl")
@RequiredArgsConstructor
public class SummonsApiImpl implements SummonsApi {

    private final SummonsService summonsService;

    @Override
    public ChannelMessage updateDeliveryStatus(RequestInfo requestInfo, ChannelReport channelReport) {
        UpdateSummonsRequest request = UpdateSummonsRequest.builder()
                .requestInfo(requestInfo)
                .channelReport(channelReport)
                .build();
        return summonsService.updateSummonsDeliveryStatus(request);
    }
}
