package org.pucar.dristi.caselifecycle.transformer.internal.controller;


import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.pucar.dristi.caselifecycle.transformer.internal.consumer.CaseConsumer;
import org.pucar.dristi.caselifecycle.transformer.internal.event.impl.OrderImpl;
import org.pucar.dristi.caselifecycle.transformer.internal.models.CaseRequest;
import org.pucar.dristi.caselifecycle.transformer.internal.models.HearingRequest;
import org.pucar.dristi.caselifecycle.transformer.internal.models.OrderRequest;
import org.pucar.dristi.caselifecycle.transformer.internal.service.CaseService;
import org.pucar.dristi.caselifecycle.transformer.internal.service.HearingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

// remove this whole class when migration is done
@Slf4j
@RestController
@RequestMapping("/transformer")
public class ScriptController {

    private final OrderImpl order;
    private final HearingService hearingService;
    private final CaseConsumer caseConsumer;

    @Autowired
    public ScriptController(OrderImpl order, HearingService hearingService, CaseConsumer caseConsumer) {
        this.order = order;
        this.hearingService = hearingService;
        this.caseConsumer = caseConsumer;
    }


    @RequestMapping(value = "/v1/script", method = RequestMethod.POST)
    public ResponseEntity<?> hearingV1CreatePost(@Valid @RequestBody OrderRequest body) {
        try {
            order.process(body.getOrder(), body.getRequestInfo());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

    }


    @RequestMapping(value = "/v1/openHearing", method = RequestMethod.POST)
    public ResponseEntity<?> openHearing(@Valid @RequestBody HearingRequest body) {
        try {
            hearingService.enrichOpenHearings(body,false);
            log.info("enriched openHearing of hearingId: {}", body.getHearing().getHearingId());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("error in openHearing", e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }

    }

    @PostMapping("/v1/script/case-request")
    public ResponseEntity<?> caseRequestV1(@Valid @RequestBody CaseRequest body) {
        try{
            caseConsumer.publishCaseSearchFromCaseRequest(body);
            return ResponseEntity.ok().build();
        }
        catch(Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
