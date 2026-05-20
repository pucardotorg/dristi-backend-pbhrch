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
import org.pucar.dristi.common.models.workflow.WorkflowObject;
import org.pucar.dristi.caselifecycle.ordermanagement.internal.web.models.adiary.CaseDiaryEntry;
import org.pucar.dristi.caselifecycle.ordermanagement.internal.web.models.courtCase.CaseCriteria;
import org.pucar.dristi.caselifecycle.ordermanagement.internal.web.models.courtCase.CaseRequest;
import org.pucar.dristi.caselifecycle.ordermanagement.internal.web.models.courtCase.CaseSearchRequest;
import org.pucar.dristi.caselifecycle.ordermanagement.internal.web.models.courtCase.CourtCase;
import org.pucar.dristi.caselifecycle.ordermanagement.internal.web.models.hearing.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.pucar.dristi.caselifecycle.ordermanagement.internal.config.ServiceConstants.*;

@Slf4j
@Component
public class PublishOrderScheduleOfHearingDate implements OrderUpdateStrategy {

    private final HearingUtil hearingUtil;
    private final Configuration configuration;
    private final CaseUtil caseUtil;
    private final PendingTaskUtil pendingTaskUtil;

    @Autowired
    public PublishOrderScheduleOfHearingDate(HearingUtil hearingUtil, Configuration configuration, CaseUtil caseUtil, PendingTaskUtil pendingTaskUtil) {
        this.hearingUtil = hearingUtil;
        this.configuration = configuration;
        this.caseUtil = caseUtil;
        this.pendingTaskUtil = pendingTaskUtil;
    }

    @Override
    public boolean supportsPreProcessing(OrderRequest orderRequest) {
        Order order = orderRequest.getOrder();
        String action = order.getWorkflow().getAction();
        return order.getOrderType() != null && E_SIGN.equalsIgnoreCase(action) && SCHEDULE_OF_HEARING_DATE.equalsIgnoreCase(order.getOrderType());
    }

    @Override
    public boolean supportsPostProcessing(OrderRequest orderRequest) {
        Order order = orderRequest.getOrder();
        String action = order.getWorkflow().getAction();
        return order.getOrderType() != null && E_SIGN.equalsIgnoreCase(action) && SCHEDULE_OF_HEARING_DATE.equalsIgnoreCase(order.getOrderType());
    }

    @Override
    public OrderRequest preProcess(OrderRequest orderRequest) {

        Order order = orderRequest.getOrder();
        RequestInfo requestInfo = orderRequest.getRequestInfo();
        log.info("pre processing, result= IN_PROGRESS,orderNumber:{}, orderType:{}", order.getOrderNumber(), order.getOrderType());

        // Create hearing
        List<CourtCase> cases = caseUtil.getCaseDetailsForSingleTonCriteria(CaseSearchRequest.builder()
                .criteria(Collections.singletonList(CaseCriteria.builder().filingNumber(order.getFilingNumber()).tenantId(order.getTenantId()).defaultFields(false).build()))
                .requestInfo(requestInfo).build());

        // add validation here
        CourtCase courtCase = cases.get(0);
        log.info("create hearing for caseId:{}", courtCase.getId());
        HearingRequest request = hearingUtil.createHearingRequestForScheduleNextHearingAndScheduleOfHearingDate(requestInfo, order, courtCase);
        StringBuilder createHearingUri = new StringBuilder(configuration.getHearingHost()).append(configuration.getHearingCreateEndPoint());
        HearingResponse createdHearingResponse = hearingUtil.createOrUpdateHearing(request, createHearingUri);
        order.setScheduledHearingNumber(createdHearingResponse.getHearing().getHearingId());
        log.info("created hearing for caseId:{}, hearingId:{}", courtCase.getId(), createdHearingResponse.getHearing().getHearingId());

        log.info("pre processing, result= SUCCESS,orderNumber:{}, orderType:{}", order.getOrderNumber(), order.getOrderType());

        return null;
    }

    @Override
    public OrderRequest postProcess(OrderRequest orderRequest) {

        Order order = orderRequest.getOrder();
        RequestInfo requestInfo = orderRequest.getRequestInfo();
        log.info("post processing, result= IN_PROGRESS,orderNumber:{}, orderType:{}", order.getOrderNumber(), order.getOrderType());

        // case update
        List<CourtCase> cases = caseUtil.getCaseDetailsForSingleTonCriteria(CaseSearchRequest.builder()
                .criteria(Collections.singletonList(CaseCriteria.builder().filingNumber(order.getFilingNumber()).tenantId(order.getTenantId()).defaultFields(false).build()))
                .requestInfo(requestInfo).build());

        // add validation here
        CourtCase courtCase = cases.get(0);
        String status = courtCase.getStatus();

        log.info("case status:{}", status);


        // if any abandon hearing is there close the hearing and close pending task for that hearing number
        List<Hearing> hearings = hearingUtil.fetchHearing(HearingSearchRequest.builder()
                .criteria(HearingCriteria.builder().tenantId(order.getTenantId())
                        .filingNumber(order.getFilingNumber()).build()).requestInfo(requestInfo).build());

        List<Hearing> abandonHearings = Optional.ofNullable(hearings).orElse(Collections.emptyList()).stream().filter(hearing -> ABANDONED.equalsIgnoreCase(hearing.getStatus())).toList();

        if (!abandonHearings.isEmpty()) {
            StringBuilder hearingUpdateUri = new StringBuilder(configuration.getHearingHost()).append(configuration.getHearingUpdateEndPoint());
            log.info("Abandoning the hearings");
            for (Hearing hearing : abandonHearings) {
                WorkflowObject workflowObject = new WorkflowObject();
                workflowObject.setAction(CLOSE);
                hearing.setWorkflow(workflowObject);
                HearingRequest request = HearingRequest.builder().requestInfo(requestInfo).hearing(hearing).build();
                hearingUtil.createOrUpdateHearing(request, hearingUpdateUri);
            }
        }

        // close manual pending task for filing number
        log.info("close manual pending task for hearing number:{}", order.getHearingNumber());
        pendingTaskUtil.closeManualPendingTask(order.getHearingNumber(), requestInfo, courtCase.getFilingNumber(), courtCase.getCnrNumber(), courtCase.getId().toString(), courtCase.getCaseTitle());
        // close manual pending task of schedule of hearing
        log.info("close manual pending task of schedule of hearing");
        pendingTaskUtil.closeManualPendingTask(order.getFilingNumber() + SCHEDULE_HEARING_SUFFIX, requestInfo, courtCase.getFilingNumber(), courtCase.getCnrNumber(), courtCase.getId().toString(), courtCase.getCaseTitle());

        log.info("post processing, result= SUCCESS,orderNumber:{}, orderType:{}", order.getOrderNumber(), order.getOrderType());

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
