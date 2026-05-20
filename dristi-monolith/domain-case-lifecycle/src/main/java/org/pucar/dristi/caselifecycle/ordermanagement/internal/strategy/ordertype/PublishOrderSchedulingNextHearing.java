package org.pucar.dristi.caselifecycle.ordermanagement.internal.strategy.ordertype;

import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.pucar.dristi.caselifecycle.ordermanagement.internal.config.Configuration;
import org.pucar.dristi.caselifecycle.ordermanagement.internal.strategy.OrderUpdateStrategy;
import org.pucar.dristi.caselifecycle.ordermanagement.internal.util.CaseUtil;
import org.pucar.dristi.caselifecycle.ordermanagement.internal.util.HearingUtil;
import org.pucar.dristi.caselifecycle.ordermanagement.internal.util.PendingTaskUtil;
import org.pucar.dristi.caselifecycle.ordermanagement.internal.web.models.Order;
import org.pucar.dristi.caselifecycle.ordermanagement.internal.web.models.OrderRequest;
import org.pucar.dristi.caselifecycle.ordermanagement.internal.web.models.adiary.CaseDiaryEntry;
import org.pucar.dristi.caselifecycle.ordermanagement.internal.web.models.courtCase.CaseCriteria;
import org.pucar.dristi.caselifecycle.ordermanagement.internal.web.models.courtCase.CaseSearchRequest;
import org.pucar.dristi.caselifecycle.ordermanagement.internal.web.models.courtCase.CourtCase;
import org.pucar.dristi.caselifecycle.ordermanagement.internal.web.models.hearing.*;

import java.util.Collections;
import java.util.List;

import static org.pucar.dristi.caselifecycle.ordermanagement.internal.config.ServiceConstants.*;

@Component
@Slf4j
public class PublishOrderSchedulingNextHearing implements OrderUpdateStrategy {

    private final HearingUtil hearingUtil;
    private final Configuration configuration;
    private final CaseUtil caseUtil;
    private final PendingTaskUtil pendingTaskUtil;

    @Autowired
    public PublishOrderSchedulingNextHearing(HearingUtil hearingUtil, Configuration configuration, CaseUtil caseUtil, PendingTaskUtil pendingTaskUtil) {
        this.hearingUtil = hearingUtil;
        this.configuration = configuration;
        this.caseUtil = caseUtil;
        this.pendingTaskUtil = pendingTaskUtil;
    }

    @Override
    public boolean supportsPreProcessing(OrderRequest orderRequest) {
        Order order = orderRequest.getOrder();
        String action = order.getWorkflow().getAction();
        return order.getOrderType() != null && E_SIGN.equalsIgnoreCase(action) && SCHEDULING_NEXT_HEARING.equalsIgnoreCase(order.getOrderType());
    }

    @Override
    public boolean supportsPostProcessing(OrderRequest orderRequest) {
        Order order = orderRequest.getOrder();
        String action = order.getWorkflow().getAction();
        return order.getOrderType() != null && E_SIGN.equalsIgnoreCase(action) && SCHEDULING_NEXT_HEARING.equalsIgnoreCase(order.getOrderType());
    }

    @Override
    public OrderRequest preProcess(OrderRequest orderRequest) {

        Order order = orderRequest.getOrder();
        RequestInfo requestInfo = orderRequest.getRequestInfo();
        log.info("pre processing, result=IN_PROGRESS,orderNumber:{}, orderType:{}", order.getOrderNumber(), SCHEDULING_NEXT_HEARING);

        List<CourtCase> cases = caseUtil.getCaseDetailsForSingleTonCriteria(CaseSearchRequest.builder()
                .criteria(Collections.singletonList(CaseCriteria.builder().filingNumber(order.getFilingNumber()).tenantId(order.getTenantId()).defaultFields(false).build()))
                .requestInfo(requestInfo).build());

        // add validation here
        CourtCase courtCase = cases.get(0);

        HearingRequest request = hearingUtil.createHearingRequestForScheduleNextHearingAndScheduleOfHearingDate(requestInfo, order, courtCase);

        StringBuilder createHearingURI = new StringBuilder(configuration.getHearingHost()).append(configuration.getHearingCreateEndPoint());

        HearingResponse newHearing = hearingUtil.createOrUpdateHearing(request, createHearingURI);

        order.setHearingNumber(newHearing.getHearing().getHearingId());
        log.info("hearing number:{}", newHearing.getHearing().getHearingId());

        log.info("pre processing, result=SUCCESS,orderNumber:{}, orderType:{}", order.getOrderNumber(), SCHEDULING_NEXT_HEARING);

        return null;
    }

    @Override
    public OrderRequest postProcess(OrderRequest orderRequest) {

        hearingUtil.updateHearingStatus(orderRequest);

        Order order = orderRequest.getOrder();
        RequestInfo requestInfo = orderRequest.getRequestInfo();


        List<CourtCase> cases = caseUtil.getCaseDetailsForSingleTonCriteria(CaseSearchRequest.builder()
                .criteria(Collections.singletonList(CaseCriteria.builder().filingNumber(order.getFilingNumber()).tenantId(order.getTenantId()).defaultFields(false).build()))
                .requestInfo(requestInfo).build());

        CourtCase courtCase = cases.get(0);
        pendingTaskUtil.closeManualPendingTask(order.getFilingNumber() + SCHEDULE_HEARING_SUFFIX, requestInfo, courtCase.getFilingNumber(), courtCase.getCnrNumber(), courtCase.getId().toString(), courtCase.getCaseTitle());


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
