package org.pucar.dristi.caselifecycle.evidence;

import org.egov.common.contract.request.RequestInfo;
import org.pucar.dristi.common.contract.evidence.EvidenceSearchCriteria;
import org.pucar.dristi.common.contract.evidence.EvidenceSearchResponse;
import org.pucar.dristi.common.contract.evidence.Pagination;

/**
 * Public, cross-subdomain API of the evidence subdomain. Other modules
 * (cases today) consume evidence through this interface — never by
 * importing from {@code internal/}.
 *
 * <p>Evidence's contract DTOs live in
 * {@code dristi-common/contract/evidence/} (lifted by Phase 35 during
 * the evidence migration). New methods here should consume those types
 * directly.
 *
 * <p>Write-side cases→evidence flows (e.g. case-registration auto-creating
 * vakalatnama / deposition artifacts) intentionally remain over REST for
 * now per Rule 35. Lifting them onto this Api would make evidence the
 * first {@code *Api} in the tree to expose a write, and that boundary
 * decision is deferred until we have a deliberate design pass on
 * cross-subdomain write semantics.
 */
public interface EvidenceApi {

    /**
     * Returns the artifacts matching the given criteria, wrapped in the
     * same {@link EvidenceSearchResponse} envelope the HTTP controller
     * would have returned so callers swapping from REST keep their
     * downstream parsing logic.
     *
     * @param requestInfo eGov request envelope carrying caller identity
     * @param criteria    search filter (caseId / filingNumber / hearing / order / ...)
     * @param pagination  optional paging hints; pass {@code null} for unpaged
     */
    EvidenceSearchResponse searchEvidence(RequestInfo requestInfo,
                                          EvidenceSearchCriteria criteria,
                                          Pagination pagination);
}
