package org.pucar.dristi.caselifecycle.ordermanagement.internal.factory;

import org.pucar.dristi.caselifecycle.ordermanagement.internal.service.OrderProcessor;

public interface OrderFactory {

    OrderProcessor createProcessor();

}
