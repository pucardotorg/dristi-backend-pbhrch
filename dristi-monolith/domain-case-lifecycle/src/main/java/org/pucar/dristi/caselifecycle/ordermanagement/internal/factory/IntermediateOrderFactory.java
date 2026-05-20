package org.pucar.dristi.caselifecycle.ordermanagement.internal.factory;

import org.springframework.stereotype.Component;
import org.pucar.dristi.caselifecycle.ordermanagement.internal.service.IntermediateOrderService;
import org.pucar.dristi.caselifecycle.ordermanagement.internal.service.OrderProcessor;

@Component
public class IntermediateOrderFactory implements OrderFactory{

    private final IntermediateOrderService intermediateOrderService;

    public IntermediateOrderFactory(IntermediateOrderService intermediateOrderService) {
        this.intermediateOrderService = intermediateOrderService;
    }

    @Override
    public OrderProcessor createProcessor() {
        return intermediateOrderService;
    }
}
