package org.pucar.dristi.integration.njdg.internal.service.interfaces;

import org.pucar.dristi.integration.njdg.internal.model.NJDGTransformRecord;
import org.pucar.dristi.integration.njdg.internal.model.cases.CourtCase;
import org.egov.common.contract.request.RequestInfo;

/**
 * Interface for transforming CourtCase to NJDG format
 * Follows Open/Closed Principle - open for extension, closed for modification
 */
public interface CaseTransformer {
    
    /**
     * Transform a CourtCase to NJDG format
     * @param courtCase the court case to transform
     * @param requestInfo request information
     * @return transformed NJDG record
     */
    NJDGTransformRecord transform(CourtCase courtCase, RequestInfo requestInfo);
}
