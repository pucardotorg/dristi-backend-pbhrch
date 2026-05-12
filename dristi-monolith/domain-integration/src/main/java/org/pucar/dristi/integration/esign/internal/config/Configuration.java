package org.pucar.dristi.integration.esign.internal.config;

import lombok.*;
import org.egov.tracer.config.TracerConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;

@Component("esignConfiguration")
@Data
@Import({TracerConfiguration.class})
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class Configuration {

    //filestore
    @Value("${egov.filestore.host}")
    private String filestoreHost;

    @Value("${egov.filestore.search.endpoint}")
    private String filestoreSearchEndPoint;

    @Value("${egov.filestore.create.endpoint}")
    private String filestoreCreateEndPoint;

    @Value("${egov.filestore.delete.endpoint}")
    private String  filestoreDeleteEndPoint;


    //ESign
    @Value("${esing.ver}")
    private String version;

    @Value("${esing.sc}")
    private String consent;

    @Value("${esing.asp.id}")
    private String aspId;

    @Value("${esing.auth.mode}")
    private String authMode;

    @Value("${esing.response.sig.type}")
    private String responseSigType;

    @Value("${esing.response.url}")
    private String responseUrl;

    @Value("${esing.id}")
    private String id;

    @Value("${esing.hash.algorithm}")
    private String hashAlgorithm;

    @Value("${esing.doc.info}")
    private String docInfo;

    @Value("${esing.ekyc.id.type}")
    private String ekycIdType;

    @Value("${esign.create.topic}")
    private String esignCreateTopic;

    @Value("${esign.update.topic}")
    private String esignUpdateTopic;

    @Value("${esign.position.offset}")
    private Float positionOffset;

    @Value("${esign.y.coordinate.offset}")
    private float eSignYCoordinateOffset;

    // HAND-CURATED — interceptor port (PR 2)
    // redirect-flow properties used by InterceptorApiController#redirectHandler.
    // OAuth fields (oathHost/.../grantType) intentionally dropped: their only
    // consumer was the interceptor's oAuthForDristi() bounce, which existed
    // solely to authenticate the now-eliminated REST hop to e-sign-svc.
    @Value("${drishti.esign.redirect.url}")
    private String redirectUrl;

    @Value("${drishti.esign.landing.page.redirect.url}")
    private String landingPageRedirectUrl;

}
