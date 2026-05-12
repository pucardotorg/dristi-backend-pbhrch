// HAND-CURATED — interceptor port (PR 2). Rule 32 applied:
// replaced REST call to /e-sign-svc/v1/_signed with direct ESignService
// invocation. Rule 38: dropped ServiceRequestRepository.callESign helper.
// oAuthForDristi() removed entirely — its OAuth bounce existed only to
// authenticate the now-eliminated REST hop; nothing audit-load-bearing
// depended on it (kafka update payload's RequestInfo carries no userInfo
// either way, only the now-unnecessary authToken).
package org.pucar.dristi.integration.esign.internal.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.pucar.dristi.common.contract.esign.SignDocParameter;
import org.pucar.dristi.common.contract.esign.SignDocRequest;
import org.pucar.dristi.integration.esign.internal.service.ESignService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class InterceptorService {

    private final ESignService eSignService;

    @Autowired
    public InterceptorService(ESignService eSignService) {
        this.eSignService = eSignService;
    }

    public String process(String response, String espId, String tenantId, String txnId) {
        log.info("operation = process, result = IN_PROGRESS, response = {}, espId = {} , tenantId = {} , fileStoreId = {}", response, espId, tenantId, txnId);

        SignDocRequest request = SignDocRequest.builder()
                .requestInfo(RequestInfo.builder().build())
                .eSignParameter(SignDocParameter.builder()
                        .txnId(txnId).response(response).tenantId(tenantId).build())
                .build();

        String fileStoreId = eSignService.signDocWithDigitalSignature(request);

        log.info("signed fileStore id {} :", fileStoreId);
        log.info("operation = process, result = SUCCESS, response = {}, espId = {} , tenantId = {} , fileStoreId = {}", response, espId, tenantId, txnId);
        return fileStoreId;
    }
}
