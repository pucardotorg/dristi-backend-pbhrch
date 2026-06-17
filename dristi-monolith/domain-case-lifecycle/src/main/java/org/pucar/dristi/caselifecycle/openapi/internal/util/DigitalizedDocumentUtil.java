package org.pucar.dristi.caselifecycle.openapi.internal.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.pucar.dristi.caselifecycle.digitalizeddocuments.DigitalizedDocumentsApi;
import org.pucar.dristi.caselifecycle.openapi.internal.config.Configuration;
import org.pucar.dristi.caselifecycle.openapi.internal.web.models.digital_document.DigitalizedDocument;
import org.pucar.dristi.caselifecycle.openapi.internal.web.models.digital_document.DigitalizedDocumentRequest;
import org.pucar.dristi.caselifecycle.openapi.internal.web.models.digital_document.DigitalizedDocumentResponse;
import org.pucar.dristi.caselifecycle.openapi.internal.web.models.digital_document.DigitalizedDocumentSearchCriteria;
import org.pucar.dristi.caselifecycle.openapi.internal.web.models.digital_document.DigitalizedDocumentSearchResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Component("openapiDigitalizedDocumentUtil")
@Slf4j
public class DigitalizedDocumentUtil {

    private final Configuration configuration;
    private final RestTemplate restTemplate;
    private final ObjectMapper mapper;
    private final DigitalizedDocumentsApi digitalizeddocumentsApi;

    @Autowired
    public DigitalizedDocumentUtil(Configuration configuration, RestTemplate restTemplate, ObjectMapper mapper,
                                   DigitalizedDocumentsApi digitalizeddocumentsApi) {
        this.configuration = configuration;
        this.restTemplate = restTemplate;
        this.mapper = mapper;
        this.digitalizeddocumentsApi = digitalizeddocumentsApi;
    }

    public DigitalizedDocumentSearchResponse searchDigitalizeDoc(DigitalizedDocumentSearchCriteria criteria, RequestInfo requestInfo) {
        try {
            org.pucar.dristi.common.contract.digitalizeddocuments.DigitalizedDocumentSearchCriteria bridgedCriteria =
                    mapper.convertValue(criteria,
                            org.pucar.dristi.common.contract.digitalizeddocuments.DigitalizedDocumentSearchCriteria.class);
            org.pucar.dristi.common.contract.digitalizeddocuments.DigitalizedDocumentSearchRequest bridgedRequest =
                    org.pucar.dristi.common.contract.digitalizeddocuments.DigitalizedDocumentSearchRequest.builder()
                            .requestInfo(requestInfo)
                            .criteria(bridgedCriteria)
                            .build();
            List<org.pucar.dristi.common.contract.digitalizeddocuments.DigitalizedDocument> documents =
                    digitalizeddocumentsApi.search(bridgedRequest);

            DigitalizedDocumentSearchResponse response = new DigitalizedDocumentSearchResponse();
            response.setDocuments(mapper.convertValue(documents, new TypeReference<List<DigitalizedDocument>>() {}));
            response.setTotalCount(documents == null ? 0 : documents.size());
            return response;
        } catch (Exception e) {
            log.error("Error while searching for digitalized documents", e);
            throw new CustomException("EVIDENCE_SERVICE_ERROR", e.getMessage());
        }
    }

    // Rule 35: writes stay REST until cross-subdomain write semantics are designed.
    public DigitalizedDocumentResponse updateDigitalizeDoc(DigitalizedDocument digitalizedDocument, RequestInfo requestInfo) {
        StringBuilder uri = new StringBuilder();
        uri.append(configuration.getDigitalizeServiceHost()).append(configuration.getDigitalizeServiceUpdateEndpoint());

        DigitalizedDocumentRequest digitalizedDocumentRequest = DigitalizedDocumentRequest.builder()
                .requestInfo(requestInfo)
                .digitalizedDocument(digitalizedDocument)
                .build();

        try {
            Object response = restTemplate.postForObject(uri.toString(), digitalizedDocumentRequest, Map.class);
            return mapper.convertValue(response, DigitalizedDocumentResponse.class);
        } catch (Exception e) {
            log.error("Error while updating digitalized document", e);
            throw new CustomException("EVIDENCE_SERVICE_ERROR", e.getMessage());
        }
    }
}
