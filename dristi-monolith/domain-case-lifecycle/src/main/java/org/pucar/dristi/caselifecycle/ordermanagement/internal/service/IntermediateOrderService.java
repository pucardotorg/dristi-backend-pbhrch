package org.pucar.dristi.caselifecycle.ordermanagement.internal.service;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.pucar.dristi.common.contract.ordermanagement.OrderRequest;
import org.pucar.dristi.caselifecycle.ordermanagement.internal.web.models.adiary.CaseDiaryEntry;

import java.util.List;

@Service
@Slf4j
public class IntermediateOrderService implements OrderProcessor {

    private final OrderStrategyExecutor orderStrategyExecutor;

    @Autowired
    public IntermediateOrderService(OrderStrategyExecutor orderStrategyExecutor) {
        this.orderStrategyExecutor = orderStrategyExecutor;
    }

    @Override
    public void preProcessOrder(OrderRequest orderRequest) {
        orderStrategyExecutor.beforePublish(orderRequest);
    }

    @Override
    public void postProcessOrder(OrderRequest orderRequest) {
        orderStrategyExecutor.afterPublish(orderRequest);

    }

    @Override
    public List<CaseDiaryEntry> processCommonItems(OrderRequest orderRequest) {
        return orderStrategyExecutor.commonProcess(orderRequest);

    }
}
