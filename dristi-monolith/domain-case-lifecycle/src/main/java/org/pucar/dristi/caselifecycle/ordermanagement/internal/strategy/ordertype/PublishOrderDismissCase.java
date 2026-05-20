package org.pucar.dristi.caselifecycle.ordermanagement.internal.strategy.ordertype;

import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.pucar.dristi.caselifecycle.ordermanagement.internal.config.Configuration;
import org.pucar.dristi.caselifecycle.ordermanagement.internal.strategy.OrderUpdateStrategy;
import org.pucar.dristi.caselifecycle.ordermanagement.internal.util.CaseUtil;
import org.pucar.dristi.caselifecycle.ordermanagement.internal.util.HearingUtil;
import org.pucar.dristi.caselifecycle.ordermanagement.internal.web.models.Order;
import org.pucar.dristi.caselifecycle.ordermanagement.internal.web.models.OrderRequest;
import org.pucar.dristi.common.models.workflow.WorkflowObject;
import org.pucar.dristi.caselifecycle.ordermanagement.internal.web.models.adiary.CaseDiaryEntry;
import org.pucar.dristi.caselifecycle.ordermanagement.internal.web.models.courtCase.CaseCriteria;
import org.pucar.dristi.caselifecycle.ordermanagement.internal.web.models.courtCase.CaseRequest;
import org.pucar.dristi.caselifecycle.ordermanagement.internal.web.models.courtCase.CaseSearchRequest;
import org.pucar.dristi.caselifecycle.ordermanagement.internal.web.models.courtCase.CourtCase;
import org.pucar.dristi.caselifecycle.ordermanagement.internal.web.models.hearing.Hearing;
import org.pucar.dristi.caselifecycle.ordermanagement.internal.web.models.hearing.HearingCriteria;
import org.pucar.dristi.caselifecycle.ordermanagement.internal.web.models.hearing.HearingRequest;
import org.pucar.dristi.caselifecycle.ordermanagement.internal.web.models.hearing.HearingSearchRequest;

import java.util.Collections;
import java.util.List;

import static org.pucar.dristi.caselifecycle.ordermanagement.internal.config.ServiceConstants.*;

@Component
@Slf4j
public class PublishOrderDismissCase implements OrderUpdateStrategy {

    private final CaseUtil caseUtil;
    private final HearingUtil hearingUtil;
    private final Configuration config;

    @Autowired
    public PublishOrderDismissCase(CaseUtil caseUtil, HearingUtil hearingUtil, Configuration config) {
        this.caseUtil = caseUtil;
        this.hearingUtil = hearingUtil;
        this.config = config;
    }

    @Override
    public boolean supportsPreProcessing(OrderRequest orderRequest) {
        return false;
    }

    @Override
    public boolean supportsPostProcessing(OrderRequest orderRequest) {
        Order order = orderRequest.getOrder();
        String action = order.getWorkflow().getAction();
        return order.getOrderType() != null && E_SIGN.equalsIgnoreCase(action) && DISMISS_CASE.equalsIgnoreCase(order.getOrderType());
    }

    @Override
    public OrderRequest preProcess(OrderRequest orderRequest) {
        return null;
    }

    @Override
    public OrderRequest postProcess(OrderRequest orderRequest) {

        Order order = orderRequest.getOrder();
        RequestInfo requestInfo = orderRequest.getRequestInfo();
        log.info("After order publish process,result = IN_PROGRESS, orderType :{}, orderNumber:{}", order.getOrderType(), order.getOrderNumber());
        List<CourtCase> cases = caseUtil.getCaseDetailsForSingleTonCriteria(CaseSearchRequest.builder()
                .criteria(Collections.singletonList(CaseCriteria.builder().filingNumber(order.getFilingNumber()).tenantId(order.getTenantId()).defaultFields(false).build()))
                .requestInfo(requestInfo).build());

        // add validation here
        CourtCase courtCase = cases.get(0);

        WorkflowObject workflow = new WorkflowObject();
        workflow.setAction(REJECT);
        courtCase.setWorkflow(workflow);
        log.info("Dismissed the case with filing number:{},action:{}", courtCase.getFilingNumber(), workflow.getAction());
        caseUtil.updateCase(CaseRequest.builder().cases(courtCase).requestInfo(requestInfo).build());

        List<Hearing> hearings = hearingUtil.fetchHearing(HearingSearchRequest.builder()
                .criteria(HearingCriteria.builder().tenantId(order.getTenantId())
                        .filingNumber(order.getFilingNumber()).build()).requestInfo(requestInfo).build());

        StringBuilder hearingUpdateUri = new StringBuilder(config.getHearingHost()).append(config.getHearingUpdateEndPoint());

        log.info("Abandoning the hearings");
        hearings.stream()
                .filter(list -> list.getHearingType().equalsIgnoreCase(ADMISSION) && !(list.getStatus().equalsIgnoreCase(COMPLETED) || list.getStatus().equalsIgnoreCase(ABATED)))
                .findFirst().ifPresent(hearing -> {
                    WorkflowObject workflowObject = new WorkflowObject();
                    workflowObject.setAction(ABANDON);
                    hearing.setWorkflow(workflowObject);

                    log.info("hearingId:{}", hearing.getHearingId());

                    HearingRequest request = HearingRequest.builder()
                            .requestInfo(requestInfo).hearing(hearing).build();

                    hearingUtil.createOrUpdateHearing(request, hearingUpdateUri);
                });
        log.info("After order publish process,result = SUCCESS, orderType :{}, orderNumber:{}", order.getOrderType(), order.getOrderNumber());

        return null;
    }

    @Override
    public boolean supportsCommon(OrderRequest orderRequest) {
        return false;
    }

    @Override
    public CaseDiaryEntry execute(OrderRequest request) {
        return null;
    }

}
