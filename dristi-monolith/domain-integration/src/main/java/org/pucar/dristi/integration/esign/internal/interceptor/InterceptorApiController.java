// HAND-CURATED — interceptor port (PR 2). Ported from
// integration-services/esign-interceptor/.../InterceptorApiController.java.
// No context-path prefix on @RequestMapping: the original interceptor ran
// at server.contextPath=""; in the monolith every other controller carries
// a per-service prefix on its class-level mapping. The interceptor's
// callback URLs (/v1/_intercept, /v1/redirect) are pinned in the
// external eSign provider config, so they MUST remain at the bare
// path. ModelAndView "redirect:" routes through Spring Boot's default
// InternalResourceViewResolver — no extra view config needed.
package org.pucar.dristi.integration.esign.internal.interceptor;

import jakarta.annotation.Generated;
import lombok.extern.slf4j.Slf4j;
import org.pucar.dristi.integration.esign.internal.config.Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import java.net.URI;

@Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2024-07-02T12:37:46.343081666+05:30[Asia/Kolkata]")
@RestController
@RequestMapping("")
@Slf4j
public class InterceptorApiController {

    private final InterceptorService service;
    private final Configuration configs;

    @Autowired
    public InterceptorApiController(InterceptorService service, Configuration configs) {
        this.service = service;
        this.configs = configs;
    }

    @GetMapping("/v1/redirect")
    public ResponseEntity<HttpHeaders> redirectHandler(@RequestParam("result") String result,
                                                      @RequestParam("filestoreId") String filestoreId,
                                                      @RequestParam("redirectionType") String redirectionType) {
        log.info("api=/v1/redirect, result = IN_PROGRESS result = {}, filestoreId = {}, userType = {}", result, filestoreId, redirectionType);

        String redirectUri;
        if (redirectionType.equalsIgnoreCase("employee") || redirectionType.equalsIgnoreCase("citizen")) {
            redirectUri = configs.getRedirectUrl() + "/ui/" + redirectionType + "/dristi";
        } else if (redirectionType.equalsIgnoreCase("landing")) {
            redirectUri = configs.getRedirectUrl() + configs.getLandingPageRedirectUrl();
        } else {
            throw new RuntimeException("Invalid redirectionType: " + redirectionType);
        }
        redirectUri += "?result=" + result + "&filestoreId=" + filestoreId;

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create(redirectUri));
        log.info("redirectUri {}", redirectUri);
        log.info("api=/v1/redirect, result = SUCCESS result = {}, filestoreId = {}, userType = {}", result, filestoreId, redirectionType);
        return new ResponseEntity<>(headers, HttpStatus.TEMPORARY_REDIRECT);
    }

    @PostMapping("/v1/_intercept")
    public ModelAndView eSignV1Interceptor(@RequestParam("eSignResponse") String response,
                                           @RequestParam("espTxnID") String espId) {
        log.info("api=/v1/_intercept, result = IN_PROGRESS eSignResponse = {}, espTxnID = {}", response, espId);

        String filestoreId = "";
        String result = "error";

        int firstHyphenIndex = espId.indexOf("-");
        int secondHyphenIndex = espId.indexOf("-", firstHyphenIndex + 1);
        String tenantId = espId.substring(0, firstHyphenIndex);
        String pageModule = espId.substring(firstHyphenIndex + 1, secondHyphenIndex);
        String txnId = espId.substring(secondHyphenIndex + 1);
        log.info("tenantId {} ,pageModule {} , txnId {}", tenantId, pageModule, txnId);
        try {
            filestoreId = service.process(response, espId, tenantId, txnId);
            result = "success";
        } catch (Exception e) {
            log.error("Error occurred while signing the doc", e);
        }

        String redirectionType;
        if (pageModule.equals("en")) {
            redirectionType = "employee";
        } else if (pageModule.equals("ci")) {
            redirectionType = "citizen";
        } else if (pageModule.equals("lp")) {
            redirectionType = "landing";
        } else {
            throw new RuntimeException("Invalid pageModule: " + pageModule);
        }

        ModelAndView modelAndView = new ModelAndView("redirect:/v1/redirect");
        modelAndView.addObject("result", result);
        modelAndView.addObject("filestoreId", filestoreId);
        modelAndView.addObject("redirectionType", redirectionType);
        log.info("api=/v1/_intercept, result = SUCCESS eSignResponse = {}, espTxnID = {}", response, espId);
        return modelAndView;
    }
}
