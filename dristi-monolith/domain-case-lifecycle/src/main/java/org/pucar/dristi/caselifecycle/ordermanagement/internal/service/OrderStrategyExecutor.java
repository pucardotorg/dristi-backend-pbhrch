package org.pucar.dristi.caselifecycle.ordermanagement.internal.service;

import lombok.RequiredArgsConstructor;
import org.springframework.util.ObjectUtils;
import org.springframework.stereotype.Service;
import org.pucar.dristi.caselifecycle.ordermanagement.internal.strategy.OrderUpdateStrategy;
import org.pucar.dristi.common.contract.ordermanagement.OrderRequest;
import org.pucar.dristi.caselifecycle.ordermanagement.internal.web.models.adiary.CaseDiaryEntry;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class OrderStrategyExecutor {

    private final List<OrderUpdateStrategy> enrichmentStrategies;

    public void beforePublish(OrderRequest orderRequest) {
        enrichmentStrategies.stream()
                .filter(strategy -> strategy.supportsPreProcessing(orderRequest))
                .forEach(strategy -> strategy.preProcess(orderRequest));

        // we can collect here all the order request and send it for botd
    }

    public void afterPublish(OrderRequest orderRequest) {
        enrichmentStrategies.stream()
                .filter(strategy -> strategy.supportsPostProcessing(orderRequest))
                .forEach(strategy -> strategy.postProcess(orderRequest));

    }

    public List<CaseDiaryEntry> commonProcess(OrderRequest orderRequest) {

        List<CaseDiaryEntry> diaryEntries = new ArrayList<>();
        enrichmentStrategies.stream()
                .filter(strategy -> strategy.supportsCommon(orderRequest))
                .forEach((strategy) -> {
                    CaseDiaryEntry diaryEntry = strategy.execute(orderRequest);
                    if (!ObjectUtils.isEmpty(diaryEntry)) diaryEntries.add(diaryEntry);

                });

        return diaryEntries;

    }
}
