package org.pucar.dristi.caselifecycle.digitalizeddocuments;

import org.pucar.dristi.common.contract.digitalizeddocuments.DigitalizedDocument;
import org.pucar.dristi.common.contract.digitalizeddocuments.DigitalizedDocumentSearchRequest;

import java.util.List;

/**
 * Public, cross-subdomain API of the digitalizeddocuments subdomain.
 * Other modules (casemanagement today) consume digitalizeddocuments
 * through this interface — never by importing from {@code internal/}.
 *
 * <p>Digitalizeddocuments' contract DTOs live at
 * {@code dristi-common/contract/digitalizeddocuments/} (lifted by
 * Phase 35).
 */
public interface DigitalizeddocumentsApi {

    /**
     * Search digitalized documents matching the criteria in the request.
     * Returns the matched documents (equivalent to the
     * {@code documents} field of the HTTP controller's
     * {@code DigitalizedDocumentSearchResponse}).
     */
    List<DigitalizedDocument> search(DigitalizedDocumentSearchRequest request);
}
