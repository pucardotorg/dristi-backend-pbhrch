package org.pucar.dristi.caselifecycle.ordermanagement.internal.strategy;

import org.pucar.dristi.caselifecycle.ordermanagement.internal.web.models.OrderRequest;
import org.pucar.dristi.caselifecycle.ordermanagement.internal.web.models.adiary.CaseDiaryEntry;

public interface OrderUpdateStrategy {

    boolean supportsPreProcessing(OrderRequest orderRequest);

    boolean supportsPostProcessing(OrderRequest orderRequest);


    OrderRequest preProcess(OrderRequest orderRequest);

    OrderRequest postProcess(OrderRequest orderRequest);

    boolean supportsCommon(OrderRequest request);

    CaseDiaryEntry execute(OrderRequest request);

}
