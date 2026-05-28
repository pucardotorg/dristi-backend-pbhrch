package org.pucar.dristi.integration.epost.internal.util;


import lombok.extern.slf4j.Slf4j;
import org.egov.tracer.model.CustomException;
import org.pucar.dristi.common.contract.epost.ChannelReport;
import org.pucar.dristi.common.contract.epost.DeliveryStatus;
import org.pucar.dristi.common.contract.epost.EPostRequest;
import org.pucar.dristi.integration.summons.SummonsApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.pucar.dristi.integration.epost.internal.config.ServiceConstants.ERROR_WHILE_UPDATING_SUMMONS;
import static org.pucar.dristi.integration.epost.internal.config.ServiceConstants.SUMMONS_UPDATE_ERROR;

@Component("epostSummonsUtil")
@Slf4j
public class SummonsUtil {

    private final SummonsApi summonsApi;

    @Autowired
    public SummonsUtil(SummonsApi summonsApi) {
        this.summonsApi = summonsApi;
    }

    public Object updateSummonsDeliveryStatus(EPostRequest request) {
        org.pucar.dristi.common.contract.summons.ChannelReport channelReport =
                org.pucar.dristi.common.contract.summons.ChannelReport.builder()
                        .deliveryStatus(adaptDeliveryStatus(enrichDeliveryStatus(request.getEPostTracker().getDeliveryStatus())))
                        .processNumber(request.getEPostTracker().getProcessNumber())
                        .taskNumber(request.getEPostTracker().getTaskNumber())
                        .remarks(request.getEPostTracker().getRemarks())
                        .build();
        try {
            return summonsApi.updateDeliveryStatus(request.getRequestInfo(), channelReport);
        } catch (Exception e) {
            log.error("Error occurred when sending Process Request ", e);
            throw new CustomException(SUMMONS_UPDATE_ERROR, ERROR_WHILE_UPDATING_SUMMONS);
        }
    }

    private DeliveryStatus enrichDeliveryStatus(DeliveryStatus deliveryStatus) {
        if (deliveryStatus.equals(DeliveryStatus.DELIVERED) || deliveryStatus.equals(DeliveryStatus.DELIVERED_TO_REDIRECT_ADDRESS)) {
            return DeliveryStatus.DELIVERED;
        } else if (deliveryStatus.equals(DeliveryStatus.REFUSED) || deliveryStatus.equals(DeliveryStatus.ADDRESS_MOVED) || deliveryStatus.equals(DeliveryStatus.ADDRESS_LEFT_WITHOUT_INSTRUCTION)
                || deliveryStatus.equals(DeliveryStatus.INSUFFICIENT_ADDRESS) || deliveryStatus.equals(DeliveryStatus.WRONG_ADDRESS) || deliveryStatus.equals(DeliveryStatus.NO_SUCH_PERSON_IN_ADDRESS)
                || deliveryStatus.equals(DeliveryStatus.DECEASED) || deliveryStatus.equals(DeliveryStatus.ADDRESS_MISSING) || deliveryStatus.equals(DeliveryStatus.UNCLAIMED) || deliveryStatus.equals(DeliveryStatus.MISSENT)
        ) {
            return DeliveryStatus.NOT_DELIVERED;
        } else {
            return DeliveryStatus.INTERMEDIATE;
        }
    }

    private org.pucar.dristi.common.contract.summons.DeliveryStatus adaptDeliveryStatus(DeliveryStatus epostStatus) {
        return org.pucar.dristi.common.contract.summons.DeliveryStatus.valueOf(epostStatus.name());
    }
}
