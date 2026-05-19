package org.pucar.dristi.caselifecycle.scheduler.internal.web.controllers;


import org.pucar.dristi.caselifecycle.scheduler.internal.service.CalendarService;
import org.pucar.dristi.common.util.ResponseInfoFactory;
import org.pucar.dristi.caselifecycle.scheduler.internal.web.models.*;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@jakarta.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2024-04-15T13:15:39.759211883+05:30[Asia/Kolkata]")
@RestController("judgeCalendarApiController")
@RequestMapping("/scheduler")
@Slf4j
public class JudgeCalendarApiController {


    private final CalendarService calendarService;

    private final ResponseInfoFactory responseInfoFactory;

    @Autowired
    public JudgeCalendarApiController(CalendarService calendarService, ResponseInfoFactory responseInfoFactory) {
        this.calendarService = calendarService;
        this.responseInfoFactory = responseInfoFactory;
    }


    @RequestMapping(value = "/judge/v1/_calendar", method = RequestMethod.POST)
    public ResponseEntity<JudgeCalendarResponse> getJudgeCalendar(@Parameter(in = ParameterIn.DEFAULT, description = "Judge calendar search criteria and Request info", required = true, schema = @Schema()) @Valid @RequestBody JudgeCalendarSearchRequest request) {
        log.info("api=/judge/v1/_calendar, result = IN_PROGRESS");
        List<HearingCalendar> judgeCalendar = calendarService.getJudgeCalendar(request);
        JudgeCalendarResponse response = JudgeCalendarResponse.builder().calendar(judgeCalendar).responseInfo(responseInfoFactory.createResponseInfo(request.getRequestInfo(), true)).build();
        log.info("api=/judge/v1/_calendar, result = SUCCESS");
        return ResponseEntity.accepted().body(response);
    }

    @RequestMapping(value = "/judge/v1/_availability", method = RequestMethod.POST)
    public ResponseEntity<JudgeAvailabilityResponse> getAvailabilityOfJudge(@Parameter(in = ParameterIn.DEFAULT, description = "Judge availability search criteria and Request info", required = true, schema = @Schema()) @Valid @RequestBody JudgeAvailabilitySearchRequest request) {
        log.info("api=/judge/v1/_availability, result = IN_PROGRESS");
        List<AvailabilityDTO> judgeAvailability = calendarService.getJudgeAvailability(request);
        JudgeAvailabilityResponse response = JudgeAvailabilityResponse.builder().responseInfo(responseInfoFactory.createResponseInfo(request.getRequestInfo(), true)).availableDates(judgeAvailability).build();
        log.info("api=/judge/v1/_availability, result = SUCCESS");
        return ResponseEntity.accepted().body(response);
    }


    @RequestMapping(value = "/judge/v1/_update", method = RequestMethod.POST)
    public ResponseEntity<?> updateJudgeCalendar(@Parameter(in = ParameterIn.DEFAULT, description = "Details for the judge calendar data to be updated.", required = true, schema = @Schema()) @Valid @RequestBody JudgeCalendarUpdateRequest request) {
        log.info("api=/judge/v1/_update, result = IN_PROGRESS");
        List<JudgeCalendarRule> updatedJudgeCalendarRule = calendarService.upsert(request);
        log.info("api=/judge/v1/_update, result = SUCCESS");
        return ResponseEntity.accepted().body(updatedJudgeCalendarRule);
    }

    @RequestMapping(value = "/calendar/v1/_search", method = RequestMethod.POST)
    public ResponseEntity<JudgeRuleResponse> getJudgeRule(@Parameter(in = ParameterIn.DEFAULT, description = "Judge calendar search criteria and Request info", required = true, schema = @Schema()) @Valid @RequestBody JudgeCalenderSearchRequest request) {
        log.info("api=/calendar/v1/_search, result = IN_PROGRESS");
        List<JudgeCalendarRule> judgeCalendar = calendarService.getJudgeRule(request);
        JudgeRuleResponse response = JudgeRuleResponse.builder().judgeCalendarRules(judgeCalendar).ResponseInfo(responseInfoFactory.createResponseInfo(request.getRequestInfo(), true)).build();
        log.info("api=/calendar/v1/_search, result = SUCCESS");
        return ResponseEntity.accepted().body(response);
    }

}
