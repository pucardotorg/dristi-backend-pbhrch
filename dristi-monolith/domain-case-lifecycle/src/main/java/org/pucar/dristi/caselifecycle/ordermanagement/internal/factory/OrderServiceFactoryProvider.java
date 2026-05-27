package org.pucar.dristi.caselifecycle.ordermanagement.internal.factory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

import static org.pucar.dristi.caselifecycle.ordermanagement.internal.config.ServiceConstants.COMPOSITE;
import static org.pucar.dristi.caselifecycle.ordermanagement.internal.config.ServiceConstants.INTERMEDIATE;

@Component
public class OrderServiceFactoryProvider {

    private final Map<String, OrderFactory> factoryMap = new HashMap<>();

    @Autowired
    public OrderServiceFactoryProvider(
            CompositeOrderFactory compositeOrderFactory,
            IntermediateOrderFactory intermediateOrderFactory) {
        factoryMap.put(COMPOSITE, compositeOrderFactory);
        factoryMap.put(INTERMEDIATE, intermediateOrderFactory);
    }

    public OrderFactory getFactory(String type) {
        return factoryMap.get(type);
    }
}
