package org.pucar.dristi.caselifecycle.digitalizeddocuments.internal.service;

import org.pucar.dristi.caselifecycle.digitalizeddocuments.DigitalizeddocumentsApi;
import org.pucar.dristi.common.contract.digitalizeddocuments.DigitalizedDocument;
import org.pucar.dristi.common.contract.digitalizeddocuments.DigitalizedDocumentSearchRequest;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DigitalizeddocumentsApiImpl implements DigitalizeddocumentsApi {

    private final DigitalizedDocumentService digitalizedDocumentService;

    public DigitalizeddocumentsApiImpl(DigitalizedDocumentService digitalizedDocumentService) {
        this.digitalizedDocumentService = digitalizedDocumentService;
    }

    @Override
    public List<DigitalizedDocument> search(DigitalizedDocumentSearchRequest request) {
        return digitalizedDocumentService.searchDigitalizedDocument(request);
    }
}
