// HAND-CURATED — interceptor port (PR 2). Rule 36: test mocks
// ESignService directly (the Rule 32 collaborator) rather than the
// pre-Rule-32 RestTemplate / ServiceRequestRepository / OAuth chain.
package org.pucar.dristi.integration.esign.internal.interceptor;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pucar.dristi.common.contract.esign.SignDocRequest;
import org.pucar.dristi.integration.esign.internal.service.ESignService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InterceptorServiceTest {

    @Mock
    private ESignService eSignService;

    @InjectMocks
    private InterceptorService interceptorService;

    @Test
    void process_callsESignServiceDirectly_andReturnsSignedFileStoreId() {
        when(eSignService.signDocWithDigitalSignature(any(SignDocRequest.class)))
                .thenReturn("signed-fs-123");

        String result = interceptorService.process("esp-response", "kl-en-txn123", "kl", "txn123");

        assertEquals("signed-fs-123", result);
    }

    @Test
    void process_passesTxnIdAndTenantIdAndResponseDownstream() {
        when(eSignService.signDocWithDigitalSignature(any(SignDocRequest.class)))
                .thenReturn("fs-x");

        interceptorService.process("response-body", "kl-en-T1", "kl", "T1");

        ArgumentCaptor<SignDocRequest> captor = ArgumentCaptor.forClass(SignDocRequest.class);
        verify(eSignService).signDocWithDigitalSignature(captor.capture());
        SignDocRequest sent = captor.getValue();
        assertEquals("T1", sent.getESignParameter().getTxnId());
        assertEquals("kl", sent.getESignParameter().getTenantId());
        assertEquals("response-body", sent.getESignParameter().getResponse());
    }
}
