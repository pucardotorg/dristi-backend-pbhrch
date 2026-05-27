package org.pucar.dristi.caselifecycle.scheduler.internal.validator;

import org.pucar.dristi.caselifecycle.scheduler.internal.util.DateUtil;
import org.pucar.dristi.common.contract.scheduler.BulkRescheduleRequest;
import org.pucar.dristi.common.contract.scheduler.BulkReschedule;
import lombok.extern.slf4j.Slf4j;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;


@Component
@Slf4j
public class ReScheduleRequestValidator {

    private final DateUtil dateUtil;

    @Autowired
    public ReScheduleRequestValidator(DateUtil dateUtil) {
        this.dateUtil = dateUtil;
    }


    public void validateBulkRescheduleRequest(BulkRescheduleRequest request) {


    }
}
