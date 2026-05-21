package org.pucar.dristi.caselifecycle.ordermanagement.internal.strategy.common;

import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.pucar.dristi.caselifecycle.ordermanagement.internal.strategy.OrderUpdateStrategy;
import org.pucar.dristi.caselifecycle.ordermanagement.internal.util.CaseUtil;
import org.pucar.dristi.caselifecycle.ordermanagement.internal.util.PendingTaskUtil;
import org.pucar.dristi.common.contract.ordermanagement.Order;
import org.pucar.dristi.common.contract.ordermanagement.OrderRequest;
import org.pucar.dristi.caselifecycle.ordermanagement.internal.web.models.adiary.CaseDiaryEntry;
import org.pucar.dristi.caselifecycle.ordermanagement.internal.web.models.courtCase.CaseCriteria;
import org.pucar.dristi.caselifecycle.ordermanagement.internal.web.models.courtCase.CaseSearchRequest;
import org.pucar.dristi.caselifecycle.ordermanagement.internal.web.models.courtCase.CourtCase;

import java.util.Collections;
import java.util.List;

import static org.pucar.dristi.caselifecycle.ordermanagement.internal.config.ServiceConstants.DELETE;

@Component
@Slf4j
public class DeleteOrder implements OrderUpdateStrategy {

    private final PendingTaskUtil pendingTaskUtil;
    private final CaseUtil caseUtil;

    @Autowired
    public DeleteOrder(PendingTaskUtil pendingTaskUtil, CaseUtil caseUtil) {
        this.pendingTaskUtil = pendingTaskUtil;
        this.caseUtil = caseUtil;
    }


    @Override
    public boolean supportsPreProcessing(OrderRequest orderRequest) {
        return false;
    }

    @Override
    public boolean supportsPostProcessing(OrderRequest orderRequest) {
        return false;
    }

    @Override
    public OrderRequest preProcess(OrderRequest orderRequest) {
        return null;
    }

    @Override
    public OrderRequest postProcess(OrderRequest orderRequest) {
        return null;
    }

    @Override
    public boolean supportsCommon(OrderRequest request) {
        String action = request.getOrder().getWorkflow().getAction();
        return DELETE.equalsIgnoreCase(action);
    }

    @Override
    public CaseDiaryEntry execute(OrderRequest request) {

        Order order = request.getOrder();
        RequestInfo requestInfo = request.getRequestInfo();

        //search case here

        List<CourtCase> cases = caseUtil.getCaseDetailsForSingleTonCriteria(CaseSearchRequest.builder()
                .criteria(Collections.singletonList(CaseCriteria.builder().filingNumber(order.getFilingNumber()).tenantId(order.getTenantId()).defaultFields(false).build()))
                .requestInfo(requestInfo).build());

        // add validation here
        CourtCase courtCase = cases.get(0);

        pendingTaskUtil.closeManualPendingTask(order.getOrderNumber(), requestInfo, courtCase.getFilingNumber(), courtCase.getCnrNumber(), courtCase.getId().toString(), courtCase.getCaseTitle());
        return null;
    }
}
