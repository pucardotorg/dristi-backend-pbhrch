package org.pucar.dristi.caselifecycle.scheduler.internal.validator;

import org.pucar.dristi.caselifecycle.scheduler.internal.config.Configuration;
import org.pucar.dristi.caselifecycle.scheduler.internal.config.ServiceConstants;
import org.pucar.dristi.caselifecycle.scheduler.internal.repository.RescheduleRequestOptOutRepository;
import org.pucar.dristi.caselifecycle.scheduler.internal.service.ReScheduleHearingService;
import org.pucar.dristi.caselifecycle.scheduler.internal.util.MasterDataUtil;
import org.pucar.dristi.caselifecycle.scheduler.internal.web.models.*;
import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class RescheduleRequestOptOutValidatorTest {

    @Mock
    private RescheduleRequestOptOutRepository repository;

    @Mock
    private ReScheduleHearingService reScheduleHearingService;

    @Mock
    private Configuration config;

    @Mock
    private MasterDataUtil mdmsUtil;

    @Mock
    private ServiceConstants constants;

    @InjectMocks
    private RescheduleRequestOptOutValidator validator;

    private OptOutRequest request;
    private OptOut optOut;

    @BeforeEach
    void setUp() {
        request = new OptOutRequest();
        request.setRequestInfo(new RequestInfo());
        optOut = new OptOut();
        optOut.setRescheduleRequestId("rescheduleRequestId");
        optOut.setJudgeId("judgeId");
        optOut.setCaseId("caseId");
        optOut.setOptoutDates(List.of(2L));
        optOut.setTenantId("tenantId");
        optOut.setIndividualId("individualId");

        request.setOptOut(optOut);

    }

    @Test
    public void validateRequest_Success() {
        ReScheduleHearing reScheduleHearing = ReScheduleHearing.builder().rescheduledRequestId("rescheduleRequestId").status("ACTIVE").suggestedDates(List.of(1L, 2L, 3L)).build();
        SchedulerConfig schedulerConfig = SchedulerConfig.builder().identifier("OPT_OUT_SELECTION_LIMIT").unit(1).build();
        List<SchedulerConfig> schedulerConfigList = new ArrayList<>();
        schedulerConfigList.add(schedulerConfig);
        when(reScheduleHearingService.search(any(), any(), any())).thenReturn(List.of(reScheduleHearing));
        when(mdmsUtil.getDataFromMDMS(SchedulerConfig.class, constants.SCHEDULER_CONFIG_MASTER_NAME, constants.SCHEDULER_CONFIG_MODULE_NAME)).thenReturn(schedulerConfigList);
        when(repository.getOptOut(OptOutSearchCriteria.builder().rescheduleRequestId("rescheduleRequestId").individualId("individualId").build(), null, null)).thenReturn(new ArrayList<>());

        validator.validateRequest(request);


    }

    @Test
    public void validateRequest_Inactive(){
        ReScheduleHearing reScheduleHearing = ReScheduleHearing.builder().rescheduledRequestId("rescheduleRequestId").status("INACTIVE").suggestedDates(List.of(1L, 2L, 3L)).build();
        when(reScheduleHearingService.search(any(), any(), any())).thenReturn(List.of(reScheduleHearing));
        CustomException customException = assertThrows(CustomException.class, () -> {
            validator.validateRequest(request);
        });
        assertEquals("DK_OO_REQUEST_COMPLETED", customException.getCode());
    }


}
