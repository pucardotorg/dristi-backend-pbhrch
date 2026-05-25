package org.pucar.dristi.caselifecycle.digitalizeddocuments.internal.service;

import lombok.RequiredArgsConstructor;
import org.pucar.dristi.caselifecycle.digitalizeddocuments.DigitalizedDocumentsApi;
import org.pucar.dristi.common.contract.digitalizeddocuments.DigitalizedDocument;
import org.pucar.dristi.common.contract.digitalizeddocuments.DigitalizedDocumentRequest;
import org.pucar.dristi.common.contract.digitalizeddocuments.DigitalizedDocumentSearchRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("digitalizeddocumentsApiImpl")
@RequiredArgsConstructor
public class DigitalizedDocumentsApiImpl implements DigitalizedDocumentsApi {

    private final DigitalizedDocumentService digitalizedDocumentService;

    @Override
    public List<DigitalizedDocument> search(DigitalizedDocumentSearchRequest request) {
        return digitalizedDocumentService.searchDigitalizedDocument(request);
    }

    @Override
    public DigitalizedDocument create(DigitalizedDocumentRequest request) {
        return digitalizedDocumentService.createDigitalizedDocument(request);
    }

    @Override
    public DigitalizedDocument update(DigitalizedDocumentRequest request) {
        return digitalizedDocumentService.updateDigitalizedDocument(request);
    }
}
