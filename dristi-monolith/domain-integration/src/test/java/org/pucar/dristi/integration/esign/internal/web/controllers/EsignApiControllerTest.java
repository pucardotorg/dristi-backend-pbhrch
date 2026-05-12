package org.pucar.dristi.integration.esign.internal.web.controllers;

import jakarta.servlet.http.HttpServletRequest;
import org.pucar.dristi.integration.esign.internal.service.ESignService;
import org.pucar.dristi.common.util.ResponseInfoFactory;
import org.pucar.dristi.integration.esign.internal.web.models.ESignRequest;
import org.pucar.dristi.integration.esign.internal.web.models.ESignResponse;
import org.pucar.dristi.integration.esign.internal.web.models.ESignXmlForm;
import org.pucar.dristi.integration.esign.internal.web.models.SignDocRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EsignApiControllerTest {

    @Mock
    private ESignService eSignService;

    @Mock
    private ResponseInfoFactory responseInfoFactory;

    @InjectMocks
    private EsignApiController esignApiController;

    @BeforeEach
    public void setup() {
        esignApiController = new EsignApiController(eSignService, responseInfoFactory);
    }

    @Test
    public void testSignDoc() {
        ESignRequest request = new ESignRequest();
        ESignResponse expectedResponse = ESignResponse.builder()
                .responseInfo(responseInfoFactory.createResponseInfoFromRequestInfo(request.getRequestInfo(), true))
                .eSignForm(null).build();

        ResponseEntity<ESignResponse> responseEntity = esignApiController.eSignDoc(request,mock(HttpServletRequest.class));

        assertEquals(ResponseEntity.accepted().body(expectedResponse), responseEntity);
    }

    @Test
    public void testSignedDoc() {
        SignDocRequest request = new SignDocRequest();
        String fileStoreId = "testFileStoreId";

        when(eSignService.signDocWithDigitalSignature(request)).thenReturn(fileStoreId);

        ResponseEntity<String> responseEntity = esignApiController.signedDoc(request);

        assertEquals(ResponseEntity.accepted().body(fileStoreId), responseEntity);
        verify(eSignService, times(1)).signDocWithDigitalSignature(request);
    }

}
