package org.pucar.dristi.caselifecycle.analytics.internal.web.controllers;

import jakarta.validation.Valid;
import org.pucar.dristi.caselifecycle.analytics.internal.service.BillingService;
import org.pucar.dristi.common.util.ResponseInfoFactory;
import org.pucar.dristi.common.contract.analytics.OfflinePaymentTaskRequest;
import org.pucar.dristi.common.contract.analytics.OfflinePaymentTaskResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/analytics")
public class BillingIndexApiController {

    private final BillingService billingService;

    private final ResponseInfoFactory responseInfoFactory;

    @Autowired
    public BillingIndexApiController(BillingService billingService, ResponseInfoFactory responseInfoFactory) {
        this.billingService = billingService;
        this.responseInfoFactory = responseInfoFactory;
    }

    @PostMapping("/offline-payment/_create")
    public ResponseEntity<OfflinePaymentTaskResponse> processOfflinePayment(@Valid @RequestBody OfflinePaymentTaskRequest offlinePaymentTaskRequest) {
        billingService.processOfflinePayment(offlinePaymentTaskRequest);
        return ResponseEntity.ok(OfflinePaymentTaskResponse.builder()
                .responseInfo(responseInfoFactory.createResponseInfoFromRequestInfo(offlinePaymentTaskRequest.getRequestInfo(), true))
                .offlinePaymentTask(offlinePaymentTaskRequest.getOfflinePaymentTask())
                .build());
    }

}
