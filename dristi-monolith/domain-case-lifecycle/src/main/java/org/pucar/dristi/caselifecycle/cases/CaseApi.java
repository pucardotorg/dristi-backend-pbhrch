package org.pucar.dristi.caselifecycle.cases;

import org.pucar.dristi.caselifecycle.cases.internal.web.models.CaseExistsRequest;
import org.pucar.dristi.caselifecycle.cases.internal.web.models.CaseExistsResponse;
import org.pucar.dristi.caselifecycle.cases.internal.web.models.CaseListResponse;
import org.pucar.dristi.caselifecycle.cases.internal.web.models.CaseSearchRequest;

/**
 * Public, cross-subdomain API of the cases subdomain. Other modules
 * (order today) consume cases through this interface — never by
 * importing from {@code internal/}.
 *
 * <p>Case's contract DTOs live at
 * {@code cases/internal/web/models/} (not in {@code dristi-common})
 * because that package carries persistence + validator tendrils that
 * the retro-lift to dristi-common cannot strip in one PR. The package
 * is exposed cross-module via {@code @NamedInterface("contract")}.
 * Eventual relocation is tracked in
 * {@code scripts/migration/FOLLOWUP_RETROLIFT_PATH_A.md}; until then,
 * the @NamedInterface marker keeps cross-module imports valid.
 */
public interface CaseApi {

    /**
     * For each criterion in the request, set {@code exists=true|false}
     * based on whether a case matching that criterion is found.
     */
    CaseExistsResponse exists(CaseExistsRequest request);

    /**
     * Search cases matching the criteria in the request. The matched
     * results land on {@code request.getCriteria().getResponseList()}
     * (preserving the legacy side-effect shape the HTTP controller
     * uses), and the same criteria object is returned in
     * {@link CaseListResponse#getCriteria()}.
     */
    CaseListResponse search(CaseSearchRequest request);
}
