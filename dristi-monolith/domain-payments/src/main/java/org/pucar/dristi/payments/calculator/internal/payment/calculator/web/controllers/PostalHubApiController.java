package org.pucar.dristi.payments.calculator.internal.payment.calculator.web.controllers;


import org.pucar.dristi.payments.calculator.internal.payment.calculator.service.PostalHubService;
import org.pucar.dristi.common.util.ResponseInfoFactory;
import org.pucar.dristi.payments.calculator.internal.payment.calculator.web.models.HubSearchRequest;
import org.pucar.dristi.payments.calculator.internal.payment.calculator.web.models.PostalHub;
import org.pucar.dristi.payments.calculator.internal.payment.calculator.web.models.PostalHubRequest;
import org.pucar.dristi.payments.calculator.internal.payment.calculator.web.models.PostalHubResponse;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@jakarta.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2024-06-10T14:05:42.847785340+05:30[Asia/Kolkata]")
@Controller
@RequestMapping("/payment-calculator")
@Slf4j
public class PostalHubApiController {

    private final PostalHubService postalHubService;
    private final ResponseInfoFactory responseInfoFactory;

    @Autowired
    public PostalHubApiController(PostalHubService postalHubService, ResponseInfoFactory responseInfoFactory) {
        this.postalHubService = postalHubService;
        this.responseInfoFactory = responseInfoFactory;
    }


    @PostMapping(value = "/hub/v1/_create")
    public ResponseEntity<PostalHubResponse> createHub(@Parameter(in = ParameterIn.DEFAULT, description = "", required = true, schema = @Schema()) @Valid @RequestBody PostalHubRequest request) {
        log.info("api=/hub/v1/_create, result=IN_PROGRESS");
        List<PostalHub> postalHubs = postalHubService.create(request);
        PostalHubResponse response = PostalHubResponse.builder().hubs(postalHubs).responseInfo(responseInfoFactory.createResponseInfoFromRequestInfo(request.getRequestInfo(), true)).build();
        log.info("api=/hub/v1/_create, result=SUCCESS");
        return ResponseEntity.accepted().body(response);
    }

    @PostMapping(value = "/hub/v1/_search")
    public ResponseEntity<PostalHubResponse> searchHub(@Parameter(in = ParameterIn.DEFAULT, description = "", required = true, schema = @Schema()) @Valid @RequestBody HubSearchRequest request) {
        log.info("api=/hub/v1/_search, result=IN_PROGRESS");
        List<PostalHub> search = postalHubService.search(request);

        PostalHubResponse response = PostalHubResponse.builder().hubs(search).responseInfo(responseInfoFactory.createResponseInfoFromRequestInfo(request.getRequestInfo(), true)).build();
        log.info("api=/hub/v1/_search, result=SUCCESS");
        return ResponseEntity.accepted().body(response);
    }

    @PostMapping(value = "/hub/v1/_update")
    public ResponseEntity<PostalHubResponse> updateHub(@Parameter(in = ParameterIn.DEFAULT, description = "", required = true, schema = @Schema()) @Valid @RequestBody PostalHubRequest request) {
        log.info("api=/hub/v1/_update, result=IN_PROGRESS");
        List<PostalHub> update = postalHubService.update(request);
        PostalHubResponse response = PostalHubResponse.builder().hubs(update).responseInfo(responseInfoFactory.createResponseInfoFromRequestInfo(request.getRequestInfo(), true)).build();
        log.info("api=/hub/v1/_update, result=SUCCESS");
        return ResponseEntity.accepted().body(response);

    }

}
