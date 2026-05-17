package org.pucar.dristi.integration.njdg.internal.service.interfaces;

import org.pucar.dristi.integration.njdg.internal.model.cases.CourtCase;

/**
 * Interface for processing additional case data
 * Follows Single Responsibility and Open/Closed Principles
 */
public interface DataProcessor {
    
    /**
     * Process and update extra parties for a case
     * @param courtCase the court case to process
     */
    void processExtraParties(CourtCase courtCase);
    
    /**
     * Process and update acts for a case
     * @param courtCase the court case to process
     */
    void processActs(CourtCase courtCase);
}
