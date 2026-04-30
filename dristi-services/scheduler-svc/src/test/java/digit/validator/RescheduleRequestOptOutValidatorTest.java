package digit.validator;

import digit.config.Configuration;
import digit.config.ServiceConstants;
import digit.repository.RescheduleRequestOptOutRepository;
import digit.service.ReScheduleHearingService;
import digit.util.MasterDataUtil;
import digit.web.models.*;
import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
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
        OffsetDateTime suggested1 = OffsetDateTime.now(ZoneOffset.UTC);
        OffsetDateTime suggested2 = suggested1.plusDays(1);
        OffsetDateTime suggested3 = suggested1.plusDays(2);
        ReScheduleHearing reScheduleHearing = ReScheduleHearing.builder().rescheduledRequestId("rescheduleRequestId").status("ACTIVE").suggestedDates(List.of(suggested1, suggested2, suggested3)).build();

        // Align opt-out dates with the suggested dates (matching epoch millis).
        optOut.setOptoutDates(List.of(suggested1.toInstant().toEpochMilli()));

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
        ReScheduleHearing reScheduleHearing = ReScheduleHearing.builder().rescheduledRequestId("rescheduleRequestId").status("INACTIVE").suggestedDates(List.of(OffsetDateTime.now(ZoneOffset.UTC))).build();
        when(reScheduleHearingService.search(any(), any(), any())).thenReturn(List.of(reScheduleHearing));
        CustomException customException = assertThrows(CustomException.class, () -> {
            validator.validateRequest(request);
        });
        assertEquals("DK_OO_REQUEST_COMPLETED", customException.getCode());
    }


}
