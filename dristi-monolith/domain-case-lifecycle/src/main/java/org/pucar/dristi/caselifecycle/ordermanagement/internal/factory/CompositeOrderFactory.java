package org.pucar.dristi.caselifecycle.ordermanagement.internal.factory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.pucar.dristi.caselifecycle.ordermanagement.internal.service.CompositeOrderService;
import org.pucar.dristi.caselifecycle.ordermanagement.internal.service.OrderProcessor;

@Component
public class CompositeOrderFactory implements OrderFactory{

    private final CompositeOrderService compositeOrderService;

    @Autowired
    public CompositeOrderFactory(CompositeOrderService compositeOrderService) {
        this.compositeOrderService = compositeOrderService;
    }

    @Override
    public OrderProcessor createProcessor() {
        return compositeOrderService;
    }
}
