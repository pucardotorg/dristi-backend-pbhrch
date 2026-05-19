package org.pucar.dristi.caselifecycle.casemanagement.internal.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.pucar.dristi.caselifecycle.casemanagement.internal.web.models.digitalizeddocument.DigitalizedDocument;
import org.pucar.dristi.caselifecycle.digitalizeddocuments.DigitalizeddocumentsApi;
import org.pucar.dristi.common.contract.digitalizeddocuments.DigitalizedDocumentSearchCriteria;
import org.pucar.dristi.common.contract.digitalizeddocuments.DigitalizedDocumentSearchRequest;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component("casemanagementDigitalizedDocumentUtil")
@Slf4j
public class DigitalizedDocumentUtil {

    private final ObjectMapper objectMapper;
    private final DigitalizeddocumentsApi digitalizeddocumentsApi;

    public DigitalizedDocumentUtil(ObjectMapper objectMapper, DigitalizeddocumentsApi digitalizeddocumentsApi) {
        this.objectMapper = objectMapper;
        this.digitalizeddocumentsApi = digitalizeddocumentsApi;
    }

    /**
     * Searches for digitalized documents based on criteria
     */
    public List<DigitalizedDocument> searchDigitalizedDocuments(String caseId, String courtId, RequestInfo requestInfo, String tenantId) {
        DigitalizedDocumentSearchRequest searchRequest = DigitalizedDocumentSearchRequest.builder()
                .requestInfo(requestInfo)
                .criteria(DigitalizedDocumentSearchCriteria.builder()
                        .caseId(caseId)
                        .courtId(courtId)
                        .status("COMPLETED")
                        .tenantId(tenantId)
                        .build())
                .build();
        try {
            List<org.pucar.dristi.common.contract.digitalizeddocuments.DigitalizedDocument> results =
                    digitalizeddocumentsApi.search(searchRequest);
            if (results == null || results.isEmpty()) {
                return Collections.emptyList();
            }
            log.info("Found {} digitalized documents", results.size());
            return objectMapper.convertValue(
                    results,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, DigitalizedDocument.class));
        } catch (Exception e) {
            log.error("Error while searching digitalized documents", e);
            throw new CustomException("DIGITALIZED_DOCUMENT_SEARCH_ERROR", e.getMessage());
        }
    }
}
