package org.pucar.dristi.integration.epost.internal.util;

import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.pucar.dristi.common.contract.epost.DeliveryStatus;
import org.pucar.dristi.common.contract.epost.EPostRequest;
import org.pucar.dristi.common.contract.epost.EPostTracker;
import org.pucar.dristi.common.contract.summons.ChannelMessage;
import org.pucar.dristi.common.contract.summons.ChannelReport;
import org.pucar.dristi.integration.summons.SummonsApi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import static org.pucar.dristi.integration.epost.internal.config.ServiceConstants.ERROR_WHILE_UPDATING_SUMMONS;
import static org.pucar.dristi.integration.epost.internal.config.ServiceConstants.SUMMONS_UPDATE_ERROR;

class SummonsUtilTest {

    @Mock
    private SummonsApi summonsApi;

    @InjectMocks
    private SummonsUtil summonsUtil;

    private EPostRequest request = new EPostRequest();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        RequestInfo requestInfo = new RequestInfo();
        EPostTracker ePostTracker = EPostTracker.builder()
                .deliveryStatus(DeliveryStatus.DELIVERED)
                .taskNumber("TS123")
                .processNumber("PS123").build();
        request.setEPostTracker(ePostTracker);
        request.setRequestInfo(requestInfo);
    }

    @Test
    void updateSummonsDeliveryStatus_success() {
        ChannelMessage stub = ChannelMessage.builder().acknowledgementStatus("OK").build();
        when(summonsApi.updateDeliveryStatus(any(RequestInfo.class), any(ChannelReport.class))).thenReturn(stub);

        Object response = summonsUtil.updateSummonsDeliveryStatus(request);

        assertNotNull(response);
        assertSame(stub, response);
    }

    @Test
    void updateSummonsDeliveryStatus_apiException() {
        when(summonsApi.updateDeliveryStatus(any(RequestInfo.class), any(ChannelReport.class)))
                .thenThrow(new RuntimeException("Boom"));

        CustomException thrown = assertThrows(CustomException.class, () ->
                summonsUtil.updateSummonsDeliveryStatus(request));
        assertEquals(SUMMONS_UPDATE_ERROR, thrown.getCode());
        assertEquals(ERROR_WHILE_UPDATING_SUMMONS, thrown.getMessage());
    }
}
