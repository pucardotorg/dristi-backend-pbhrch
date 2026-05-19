package org.pucar.dristi.caselifecycle.scheduler.internal.web.controllers;


import org.pucar.dristi.caselifecycle.scheduler.internal.service.HearingService;
import org.pucar.dristi.caselifecycle.scheduler.internal.service.hearing.HearingProcessor;
import org.pucar.dristi.common.util.ResponseInfoFactory;
import org.pucar.dristi.caselifecycle.scheduler.internal.web.models.*;
import org.pucar.dristi.caselifecycle.scheduler.internal.web.models.hearing.HearingRequest;
import io.swagger.annotations.ApiParam;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.List;

@jakarta.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2024-04-15T13:15:39.759211883+05:30[Asia/Kolkata]")
@RestController("hearingApiController")
@RequestMapping("/scheduler")
@Slf4j
public class HearingApiController {

    private final HearingService hearingService;

    // remove this ones its done
    private final HearingProcessor processor;

    private final ResponseInfoFactory responseInfoFactory;

    @Autowired
    public HearingApiController(HearingService hearingService, HearingProcessor processor, ResponseInfoFactory responseInfoFactory) {
        this.hearingService = hearingService;
        this.processor = processor;
        this.responseInfoFactory = responseInfoFactory;
    }


    @RequestMapping(value = "/hearing/v1/_schedule", method = RequestMethod.POST)
    public ResponseEntity<HearingResponse> scheduleHearing(@Parameter(in = ParameterIn.DEFAULT, description = "Hearing Details and Request Info", required = true, schema = @Schema()) @Valid @RequestBody ScheduleHearingRequest request) {
        log.info("api=/hearing/v1/_schedule, result = IN_PROGRESS");
        List<ScheduleHearing> scheduledHearings = hearingService.scheduleHearingInScheduler(request);
        HearingResponse response = HearingResponse.builder().hearings(scheduledHearings).responseInfo(responseInfoFactory.createResponseInfo(request.getRequestInfo(), true)).build();
        log.info("api=/hearing/v1/_schedule, result = SUCCESS");
        return ResponseEntity.accepted().body(response);
    }


    @RequestMapping(value = "/hearing/v1/_search", method = RequestMethod.POST)
    public ResponseEntity<HearingResponse> searchHearing(@Parameter(in = ParameterIn.DEFAULT, description = "Hearing Details and Request Info", required = true, schema = @Schema()) @Valid @RequestBody HearingSearchRequest request, @NotNull @Min(0) @Max(1000) @ApiParam(value = "Pagination - limit records in response", required = true) @Valid @RequestParam(value = "limit", required = true) Integer limit, @NotNull @Min(1) @ApiParam(value = "Pagination - pageNo for which response is returned", required = true) @Valid @RequestParam(value = "offset", required = true) Integer offset) {
        log.info("api=/hearing/v1/_search, result = IN_PROGRESS");
        List<ScheduleHearing> scheduledHearings = hearingService.search(request, limit, offset);
        HearingResponse response = HearingResponse.builder().responseInfo(responseInfoFactory.createResponseInfo(request.getRequestInfo(), true))
                .hearings(scheduledHearings).build();
        log.info("api=/hearing/v1/_search, result = SUCCESS");
        return ResponseEntity.accepted().body(response);
    }


    @RequestMapping(value = "/hearing/v1/_update", method = RequestMethod.POST)
    public ResponseEntity<HearingResponse> updateHearing(@Parameter(in = ParameterIn.DEFAULT, description = "Hearing Details and Request Info", required = true, schema = @Schema()) @Valid @RequestBody UpdateHearingRequest request) {
        log.info("api=/hearing/v1/_update, result = IN_PROGRESS");
        ScheduleHearing scheduledHearings = hearingService.updateHearing(request);
        HearingResponse response = HearingResponse.builder().hearings(Collections.singletonList(scheduledHearings)).responseInfo(responseInfoFactory.createResponseInfo(request.getRequestInfo(), true)).build();
        log.info("api=/hearing/v1/_update, result = SUCCESS");
        return ResponseEntity.accepted().body(response);
    }

    @RequestMapping(value = "/hearing/v1/bulk/_update", method = RequestMethod.POST)
    public ResponseEntity<HearingResponse> updateHearingList(@Parameter(in = ParameterIn.DEFAULT, description = "Hearing Details and Request Info", required = true, schema = @Schema()) @Valid @RequestBody ScheduleHearingRequest request) {
        log.info("api=/hearing/v1/bulk/_update, result = IN_PROGRESS");
        List<ScheduleHearing> scheduledHearings = hearingService.update(request);
        HearingResponse response = HearingResponse.builder().hearings(scheduledHearings).responseInfo(responseInfoFactory.createResponseInfo(request.getRequestInfo(), true)).build();
        log.info("api=/hearing/v1/bulk/_update, result = SUCCESS");
        return ResponseEntity.accepted().body(response);
    }


    @RequestMapping(value = "/script", method = RequestMethod.POST)
    public ResponseEntity<?> script(@Parameter(in = ParameterIn.DEFAULT, description = "Hearing Details and Request Info", required = true, schema = @Schema()) @Valid @RequestBody HearingRequest request) {
        log.info("api=/script, result = IN_PROGRESS");
        processor.processCreateHearingRequest(request, Boolean.FALSE);
        log.info("api=/script, result = SUCCESS");
        return new ResponseEntity<>(HttpStatus.OK);
    }


}
