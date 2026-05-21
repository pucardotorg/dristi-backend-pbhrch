package org.pucar.dristi.caselifecycle.ctc;

import org.pucar.dristi.common.contract.ctc.CtcApplication;
import org.pucar.dristi.common.contract.ctc.CtcApplicationSearchRequest;

import java.util.List;

/**
 * Public, cross-subdomain API of the ctc subdomain. Other modules
 * (casemanagement today) consume ctc through this interface — never
 * by importing from {@code internal/}.
 *
 * <p>Read-only at the moment. Writes (e.g. CTC application update)
 * remain on REST per Rule 35 — exposing cross-subdomain mutators is a
 * deferred design decision pending a deliberate pass on
 * cross-subdomain write semantics.
 *
 * <p>Ctc's contract DTOs live at
 * {@code dristi-common/contract/ctc/} (lifted by Phase 35).
 */
public interface CtcApi {

    /**
     * Search CTC applications matching the criteria in the request.
     * Returns the matched applications (equivalent to the
     * {@code ctcApplications} field of the HTTP controller's response).
     */
    List<CtcApplication> search(CtcApplicationSearchRequest request);
}
