package org.pucar.dristi.caselifecycle.cases.internal.enrichment;

import lombok.extern.slf4j.Slf4j;
import org.pucar.dristi.caselifecycle.cases.internal.enrichment.strategy.EnrichmentStrategy;
import org.pucar.dristi.caselifecycle.cases.internal.web.models.CaseRequest;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Optional;

import static org.pucar.dristi.caselifecycle.cases.internal.config.ServiceConstants.SEND_BACK;


@Component
@Slf4j
public class EnrichWhenCaseSendBack implements EnrichmentStrategy {
    @Override
    public boolean canEnrich(CaseRequest caseRequest) {
        return SEND_BACK.equalsIgnoreCase(
                caseRequest.getCases().getWorkflow().getAction());
    }

    @Override
    public void enrich(CaseRequest caseRequest) {
        log.info("Method=EnrichWhenCaseSendBack, Result=IN_PROGRESS, CaseId={},Setting hasSigned to false", caseRequest.getCases().getId());

        Optional.ofNullable(caseRequest.getCases().getLitigants()).orElse(Collections.emptyList()).forEach((litigant) -> {
            litigant.setHasSigned(false);
        });

        Optional.ofNullable(caseRequest.getCases().getRepresentatives()).orElse(Collections.emptyList()).forEach((representative) -> {
            representative.setHasSigned(false);
        });

        Optional.ofNullable(caseRequest.getCases().getPoaHolders()).orElse(Collections.emptyList()).forEach((powerOfAttorney) -> {
            powerOfAttorney.setHasSigned(false);
        });
        log.info("Method=EnrichWhenCaseSendBack, Result=SUCCESS, CaseId={}", caseRequest.getCases().getId());

    }
}
