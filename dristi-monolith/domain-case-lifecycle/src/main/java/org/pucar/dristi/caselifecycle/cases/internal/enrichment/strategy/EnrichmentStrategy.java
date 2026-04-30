package org.pucar.dristi.caselifecycle.cases.internal.enrichment.strategy;

import org.pucar.dristi.caselifecycle.cases.internal.web.models.CaseRequest;


public interface EnrichmentStrategy {

    boolean canEnrich(CaseRequest courtCase);
    void enrich(CaseRequest courtCase);
}
