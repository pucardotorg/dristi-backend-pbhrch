package org.pucar.dristi.caselifecycle.scheduler.internal.task;


import org.pucar.dristi.caselifecycle.scheduler.internal.config.Configuration;
import org.pucar.dristi.caselifecycle.scheduler.internal.config.ServiceConstants;
import org.pucar.dristi.common.kafka.Producer;
import org.pucar.dristi.caselifecycle.scheduler.internal.repository.ReScheduleRequestRepository;
import org.pucar.dristi.caselifecycle.scheduler.internal.repository.RescheduleRequestOptOutRepository;
import org.pucar.dristi.caselifecycle.scheduler.internal.service.UserService;
import org.pucar.dristi.caselifecycle.scheduler.internal.service.hearing.OptOutProcessor;
import org.pucar.dristi.caselifecycle.scheduler.internal.util.DateUtil;
import org.pucar.dristi.caselifecycle.scheduler.internal.util.MasterDataUtil;
import org.pucar.dristi.caselifecycle.scheduler.internal.util.PendingTaskUtil;
import org.pucar.dristi.caselifecycle.scheduler.internal.web.models.*;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.User;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.pucar.dristi.caselifecycle.scheduler.internal.config.ServiceConstants.INACTIVE;
import static org.pucar.dristi.caselifecycle.scheduler.internal.config.ServiceConstants.OPT_OUT_DUE;

@Component
@Slf4j
@EnableScheduling
public class RequestOptOutScheduleTask {

    private final ReScheduleRequestRepository reScheduleRepository;
    private final RescheduleRequestOptOutRepository requestOptOutRepository;
    private final Producer producer;
    private final Configuration config;
    private final MasterDataUtil mdmsUtil;
    private final ServiceConstants constants;
    private final OptOutProcessor optOutProcessor;
    private final DateUtil dateUtil;
    private final PendingTaskUtil pendingTaskUtil;
    private final UserService userService;

    @Autowired
    public RequestOptOutScheduleTask(ReScheduleRequestRepository reScheduleRepository, RescheduleRequestOptOutRepository requestOptOutRepository, Producer producer, Configuration config, MasterDataUtil mdmsUtil, ServiceConstants constants, DateUtil dateUtil, PendingTaskUtil pendingTaskUtil, OptOutProcessor optOutProcessor, UserService userService) {
        this.reScheduleRepository = reScheduleRepository;
        this.requestOptOutRepository = requestOptOutRepository;
        this.producer = producer;
        this.config = config;
        this.mdmsUtil = mdmsUtil;
        this.constants = constants;
        this.dateUtil = dateUtil;
        this.optOutProcessor = optOutProcessor;
        this.pendingTaskUtil = pendingTaskUtil;
        this.userService = userService;
    }

    @Scheduled(cron = "${drishti.cron.opt-out.due.date}", zone = "Asia/Kolkata")
    public void updateAvailableDatesFromOptOuts() {
        try {
            log.info("operation = updateAvailableDatesFromOptOuts, result=IN_PROGRESS");

            List<SchedulerConfig> dataFromMDMS = mdmsUtil.getDataFromMDMS(SchedulerConfig.class, constants.SCHEDULER_CONFIG_MASTER_NAME, constants.SCHEDULER_CONFIG_MODULE_NAME);
            List<SchedulerConfig> filteredApplications = dataFromMDMS.stream()
                    .filter(application -> application.getIdentifier().equals(OPT_OUT_DUE))
                    .toList();

            int unit = 0;
            if (!filteredApplications.isEmpty()) {
                unit = filteredApplications.get(0).getUnit();
            }

            // due date for opt out
            Long dueDate = dateUtil.getEpochFromLocalDateTime(LocalDateTime.now().minusHours(unit));
            List<ReScheduleHearing> reScheduleHearings = reScheduleRepository.getReScheduleRequest(ReScheduleHearingReqSearchCriteria.builder().tenantId(config.getEgovStateTenantId()).dueDate(dueDate).build(), null, null);

            for (ReScheduleHearing reScheduleHearing : reScheduleHearings) {
                List<OptOut> optOuts = requestOptOutRepository.getOptOut(OptOutSearchCriteria.builder().judgeId(reScheduleHearing.getJudgeId()).caseId(reScheduleHearing.getCaseId()).rescheduleRequestId(reScheduleHearing.getRescheduledRequestId()).tenantId(reScheduleHearing.getTenantId()).build(), null, null);


                List<Long> suggestedDays = new ArrayList<>(reScheduleHearing.getSuggestedDates());
                List<Long> availableDates = new ArrayList<>(suggestedDays);

                for (OptOut optOut : optOuts) {
                    List<Long> optOutDates = optOut.getOptoutDates();
                    availableDates.removeAll(optOutDates);
                }

                reScheduleHearing.setAvailableDates(availableDates);
                reScheduleHearing.setStatus(INACTIVE);

                //todo: audit details

                //open pending task for judge

                PendingTask pendingTask = pendingTaskUtil.createPendingTask(reScheduleHearing);
                PendingTaskRequest request = PendingTaskRequest.builder()
                        .pendingTask(pendingTask)
                        .requestInfo(new RequestInfo()).build();
                pendingTaskUtil.callAnalytics(request);

                //unblock judge calendar for suggested days - available days

                optOutProcessor.unblockJudgeCalendarForSuggestedDays(reScheduleHearing);
            }
            RequestInfo requestInfo = createInternalRequestInfo();
            ReScheduleHearingRequest reScheduleHearingRequest = ReScheduleHearingRequest.builder().requestInfo(requestInfo)
                    .reScheduleHearing(reScheduleHearings).build();
            producer.push(config.getUpdateRescheduleRequestTopic(), reScheduleHearingRequest);
            log.info("operation= updateAvailableDatesFromOptOuts, result=SUCCESS");
        } catch (Exception e) {
            throw new CustomException("DK_SH_APP_ERR", "Error in setting available dates.");
        }
    }

    private RequestInfo createInternalRequestInfo() {
        User userInfo = new User();
        userInfo.setUuid(userService.internalMicroserviceRoleUuid);
        return RequestInfo.builder().userInfo(userInfo).build();
    }
}
