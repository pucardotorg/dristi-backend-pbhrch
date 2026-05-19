package org.pucar.dristi.caselifecycle.scheduler.internal.web.controllers;


import org.pucar.dristi.caselifecycle.scheduler.internal.service.RescheduleRequestOptOutService;
import org.pucar.dristi.common.util.ResponseInfoFactory;
import org.pucar.dristi.common.contract.scheduler.OptOut;
import org.pucar.dristi.common.contract.scheduler.OptOutRequest;
import org.pucar.dristi.common.contract.scheduler.OptOutResponse;
import org.pucar.dristi.common.contract.scheduler.OptOutSearchRequest;
import io.swagger.annotations.ApiParam;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.List;

@RestController("rescheduleRequestOptOutApiController")
@RequestMapping("/scheduler")
@Slf4j
public class RescheduleRequestOptOutApiController {

    private final RescheduleRequestOptOutService optOutService;
    private final ResponseInfoFactory responseInfoFactory;

    @Autowired
    public RescheduleRequestOptOutApiController(RescheduleRequestOptOutService optOutService, ResponseInfoFactory responseInfoFactory) {
        this.optOutService = optOutService;
        this.responseInfoFactory = responseInfoFactory;
    }

    @RequestMapping(value = "/hearing/v1/_opt-out", method = RequestMethod.POST)
    public ResponseEntity<OptOutResponse> optOutDates(@Parameter(in = ParameterIn.DEFAULT, description = "Hearing Details and Request Info", required = true, schema = @Schema()) @Valid @RequestBody OptOutRequest request) {
        log.info("api = /hearing/v1/_opt-out, result = IN_PROGRESS");
        OptOut optOutResponse = optOutService.create(request);
        OptOutResponse response = OptOutResponse.builder().responseInfo(responseInfoFactory.createResponseInfo(request.getRequestInfo(), true))
                .optOut(optOutResponse).build();
        log.info("api = /hearing/v1/_opt-out, result = SUCCESS");
        return ResponseEntity.accepted().body(response);
    }


    @RequestMapping(value = "/hearing/v1/opt-out/_search", method = RequestMethod.POST)
    public ResponseEntity<List<OptOut>> searchOptOut(@Parameter(in = ParameterIn.DEFAULT, description = "Hearing Details and Request Info", required = true, schema = @Schema()) @Valid @RequestBody OptOutSearchRequest request, @NotNull @Min(0) @Max(1000) @ApiParam(value = "Pagination - limit records in response", required = true) @Valid @RequestParam(value = "limit", required = true) Integer limit, @NotNull @Min(1) @ApiParam(value = "Pagination - offset for which response is returned", required = true) @Valid @RequestParam(value = "offset", required = true) Integer offset) {
        log.info("api =/hearing/v1/opt-out/_search, result = IN_PROGRESS");
        List<OptOut> optOuts = optOutService.search(request, limit, offset);
        log.info("api = /hearing/v1/opt-out/_search, result = SUCCESS");
        return ResponseEntity.accepted().body(optOuts);
    }
}
