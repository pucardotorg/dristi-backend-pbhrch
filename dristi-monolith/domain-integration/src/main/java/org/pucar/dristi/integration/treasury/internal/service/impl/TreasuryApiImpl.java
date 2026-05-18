package org.pucar.dristi.integration.treasury.internal.service.impl;

import org.egov.common.contract.request.RequestInfo;
import org.pucar.dristi.common.contract.treasury.Calculation;
import org.pucar.dristi.common.contract.treasury.DemandCreateRequest;
import org.pucar.dristi.common.models.Document;
import org.pucar.dristi.integration.treasury.TreasuryApi;
import org.pucar.dristi.integration.treasury.internal.model.TreasuryMapping;
import org.pucar.dristi.integration.treasury.internal.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TreasuryApiImpl implements TreasuryApi {

    private final PaymentService paymentService;

    @Autowired
    public TreasuryApiImpl(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @Override
    public void createDemand(RequestInfo requestInfo, DemandCreateRequest request) {
        request.setRequestInfo(requestInfo);
        paymentService.createDemand(request);
    }

    @Override
    public Document getPaymentReceipt(RequestInfo requestInfo, String billId) {
        org.egov.common.contract.models.Document upstream = paymentService.getTreasuryPaymentData(billId);
        if (upstream == null) {
            return null;
        }
        return Document.builder()
                .fileStore(upstream.getFileStore())
                .build();
    }

    @Override
    public Calculation getHeadBreakDownCalculation(RequestInfo requestInfo, String consumerCode) {
        TreasuryMapping mapping = paymentService.getHeadBreakDown(consumerCode);
        if (mapping == null) {
            return null;
        }
        return mapping.getFinalCalcPostResubmission() != null
                ? mapping.getFinalCalcPostResubmission()
                : mapping.getCalculation();
    }
}
