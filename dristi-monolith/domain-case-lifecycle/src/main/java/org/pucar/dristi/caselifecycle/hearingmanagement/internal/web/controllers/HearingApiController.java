package org.pucar.dristi.caselifecycle.hearingmanagement.internal.web.controllers;


import org.pucar.dristi.caselifecycle.hearingmanagement.internal.service.HearingService;
import org.pucar.dristi.common.util.ResponseInfoFactory;
import org.pucar.dristi.common.contract.hearingmanagement.HearingSearchListResponse;
import org.pucar.dristi.common.contract.hearingmanagement.HearingSearchRequest;
import org.pucar.dristi.common.contract.hearingmanagement.HearingSearchResponse;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.validation.Valid;

import java.util.List;

@Slf4j
@jakarta.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2025-05-07T18:50:26.938815960+05:30[Asia/Kolkata]")
@Controller("hearingmanagementHearingApiController")
@RequestMapping("/hearing-management")
public class HearingApiController {

    private final HearingService hearingService;

    private final ResponseInfoFactory responseInfoFactory;

    @Autowired
    public HearingApiController(HearingService hearingService, ResponseInfoFactory responseInfoFactory) {
        this.hearingService = hearingService;
        this.responseInfoFactory = responseInfoFactory;
    }

    @RequestMapping(value = "/hearing/v1/search", method = RequestMethod.POST)
    public ResponseEntity<HearingSearchListResponse> hearingV1SearchPost(@Parameter(in = ParameterIn.DEFAULT, description = "Details for the search hearing(s) + RequestInfo meta data.", required = true, schema = @Schema()) @Valid @RequestBody HearingSearchRequest body) {
        log.info("Hearing search request: {}", body);
        HearingSearchListResponse hearingSearchListResponse = hearingService.searchHearings(body);

        hearingSearchListResponse.setResponseInfo(responseInfoFactory.createResponseInfoFromRequestInfo(body.getRequestInfo(), true));
        return new ResponseEntity<>(hearingSearchListResponse, HttpStatus.OK);
    }

}
