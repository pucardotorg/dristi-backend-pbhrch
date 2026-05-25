package org.pucar.dristi.caselifecycle.digitalizeddocuments;

import org.pucar.dristi.common.contract.digitalizeddocuments.DigitalizedDocument;
import org.pucar.dristi.common.contract.digitalizeddocuments.DigitalizedDocumentRequest;
import org.pucar.dristi.common.contract.digitalizeddocuments.DigitalizedDocumentSearchRequest;

import java.util.List;

/**
 * Public, cross-subdomain API of the digitalized-documents subdomain.
 * Other subdomains consume digitalized-documents through this interface
 * — never by importing from {@code internal/}.
 *
 * <p>Contract DTOs live at
 * {@code dristi-common/contract/digitalizeddocuments/}.
 */
public interface DigitalizedDocumentsApi {

    /**
     * Search digitalized documents by criteria — mirrors the
     * {@code /digitalized-documents/v1/_search} REST endpoint.
     */
    List<DigitalizedDocument> search(DigitalizedDocumentSearchRequest request);

    /**
     * Create a new digitalized document — mirrors the
     * {@code /digitalized-documents/v1/_create} REST endpoint.
     */
    DigitalizedDocument create(DigitalizedDocumentRequest request);

    /**
     * Update an existing digitalized document — mirrors the
     * {@code /digitalized-documents/v1/_update} REST endpoint.
     */
    DigitalizedDocument update(DigitalizedDocumentRequest request);
}
