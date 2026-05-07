package org.pucar.dristi.integration.bank.internal.web.controllers;

import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.response.ResponseInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pucar.dristi.common.contract.bank.BankDetails;
import org.pucar.dristi.common.contract.bank.BankDetailsSearchCriteria;
import org.pucar.dristi.common.contract.bank.BankDetailsSearchRequest;
import org.pucar.dristi.common.contract.bank.BankDetailsSearchResponse;
import org.pucar.dristi.common.util.ResponseInfoFactory;
import org.pucar.dristi.integration.bank.internal.service.BankDetailsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BankDetailsApiControllerTest {

    @Mock
    private BankDetailsService bankDetailsService;

    @Mock
    private ResponseInfoFactory responseInfoFactory;

    @InjectMocks
    private BankDetailsApiController controller;

    @Test
    void bankDetailsV1SearchPostSuccess() {
        BankDetails bd = BankDetails.builder().ifsc("SBIN0005094").name("Bank").branch("Branch").build();
        when(bankDetailsService.searchBankDetails(any(BankDetailsSearchRequest.class)))
                .thenReturn(Collections.singletonList(bd));
        when(responseInfoFactory.createResponseInfoFromRequestInfo(any(RequestInfo.class), Mockito.eq(true)))
                .thenReturn(ResponseInfo.builder().apiId("api").build());

        BankDetailsSearchRequest req = BankDetailsSearchRequest.builder()
                .requestInfo(new RequestInfo())
                .criteria(Collections.singletonList(BankDetailsSearchCriteria.builder().ifsc("SBIN0005094").build()))
                .build();

        ResponseEntity<BankDetailsSearchResponse> response = controller.bankDetailsV1SearchPost(req);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        List<BankDetails> bankDetails = response.getBody().getBankDetails();
        assertEquals(1, bankDetails.size());
        assertEquals("SBIN0005094", bankDetails.get(0).getIfsc());
        assertEquals("api", response.getBody().getResponseInfo().getApiId());
    }
}
