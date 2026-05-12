package org.pucar.dristi.caselifecycle.digitalizeddocuments.internal.service;

import org.pucar.dristi.common.contract.digitalizeddocuments.DigitalizedDocument;
import org.pucar.dristi.common.contract.digitalizeddocuments.DigitalizedDocumentRequest;
import org.egov.tracer.model.CustomException;

/**
 * Interface for type-specific document operations
 */
public interface DocumentTypeService {

    /**
     * Processes and validates a digitalized document based on its type
     *
     * @param request The document to process
     * @return Processed document
     * @throws CustomException if validation or processing fails
     */
    DigitalizedDocument createDocument(DigitalizedDocumentRequest request);

    /**
     * Updates and validates a digitalized document based on its type
     *
     * @param request The document to update
     * @return Updated document
     * @throws CustomException if validation or updating fails
     */
    DigitalizedDocument updateDocument(DigitalizedDocumentRequest request);

}
