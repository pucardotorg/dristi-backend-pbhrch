package org.pucar.dristi.caselifecycle.cases.internal.enrichment;

import lombok.RequiredArgsConstructor;
import org.pucar.dristi.caselifecycle.cases.internal.enrichment.strategy.EnrichmentStrategy;
import org.pucar.dristi.caselifecycle.cases.internal.web.models.CaseRequest;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
public class EnrichmentService  {

    private final List<EnrichmentStrategy> enrichmentStrategies;

    public void enrichCourtCase(CaseRequest courtCase) {
        enrichmentStrategies.stream()
                .filter(strategy -> strategy.canEnrich(courtCase))
                .forEach(strategy -> strategy.enrich(courtCase));
    }
}
