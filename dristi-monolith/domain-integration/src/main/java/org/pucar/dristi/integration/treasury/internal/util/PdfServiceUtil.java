package org.pucar.dristi.integration.treasury.internal.util;


import lombok.extern.slf4j.Slf4j;
import org.pucar.dristi.integration.treasury.internal.config.PaymentConfiguration;
import org.pucar.dristi.integration.treasury.internal.model.TreasuryPaymentRequest;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import static org.pucar.dristi.integration.treasury.internal.config.ServiceConstants.PDFSERVICE_UTILITY_EXCEPTION;

@Component("treasuryPdfServiceUtil")
@Slf4j
public class PdfServiceUtil {

    private final RestTemplate restTemplate;

    private final PaymentConfiguration config;

    @Autowired
    public PdfServiceUtil(RestTemplate restTemplate, PaymentConfiguration config) {
        this.restTemplate = restTemplate;
        this.config = config;
    }

    public ByteArrayResource generatePdfFromPdfService(TreasuryPaymentRequest pdfRequest) {
        try {
            StringBuilder uri = new StringBuilder();
            uri.append(config.getPdfServiceHost())
                    .append(config.getPdfServiceEndpoint())
                    .append("?tenantId=").append(config.getEgovStateTenantId()).append("&key=").append(config.getPdfTemplateKey());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<TreasuryPaymentRequest> requestEntity = new HttpEntity<>(pdfRequest, headers);

            ResponseEntity<ByteArrayResource> responseEntity = restTemplate.postForEntity(uri.toString(),
                    requestEntity, ByteArrayResource.class);

            return responseEntity.getBody();
        } catch (Exception e) {
            log.error("Error getting response from Pdf Service", e);
            throw new CustomException(PDFSERVICE_UTILITY_EXCEPTION, "Error getting response from Pdf Service");
        }
    }
}
