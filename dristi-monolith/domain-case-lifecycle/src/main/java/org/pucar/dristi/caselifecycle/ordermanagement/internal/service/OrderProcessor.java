package org.pucar.dristi.caselifecycle.ordermanagement.internal.service;

import org.pucar.dristi.caselifecycle.ordermanagement.internal.web.models.OrderRequest;
import org.pucar.dristi.caselifecycle.ordermanagement.internal.web.models.adiary.CaseDiaryEntry;

import java.util.List;

public interface OrderProcessor {

    void preProcessOrder(OrderRequest request);
    void postProcessOrder(OrderRequest request);
    List<CaseDiaryEntry> processCommonItems(OrderRequest request);


}
