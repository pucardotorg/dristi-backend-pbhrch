package org.pucar.dristi.caselifecycle.bailbond;

import org.pucar.dristi.common.contract.bailbond.BailSearchRequest;
import org.pucar.dristi.common.contract.bailbond.BailSearchResponse;

/**
 * Public, cross-subdomain API of the bailbond subdomain. Other modules
 * (openapi today) consume bailbond through this interface — never by
 * importing from {@code internal/}.
 *
 * <p>Bailbond's contract DTOs live in
 * {@code dristi-common/contract/bailbond/} (lifted by Phase 35 during
 * the bailbond migration), exposed via
 * {@code @NamedInterface("contract-bailbond")}. New methods here should
 * consume those types directly.
 *
 * <p>Write-side flows (bail create/update, sign) intentionally remain over
 * REST for now per Rule 35 — every existing {@code *Api} in the tree is
 * read-only, and lifting a write here is a deferred Tier 3 design decision.
 */
public interface BailbondApi {

    /**
     * Searches bail bonds matching the given criteria, wrapped in the same
     * {@link BailSearchResponse} envelope the HTTP controller
     * ({@code /bail-bond/v1/_search}) would have returned so callers
     * swapping from REST keep their downstream parsing logic.
     *
     * @param request bail search payload — {@code RequestInfo}, criteria and
     *                optional pagination
     */
    BailSearchResponse searchBail(BailSearchRequest request);
}
