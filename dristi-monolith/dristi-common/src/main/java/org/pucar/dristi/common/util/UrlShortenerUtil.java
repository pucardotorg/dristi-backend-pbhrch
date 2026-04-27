// HAND-CURATED — imports rewired to dristi-common; do not regenerate
// AUTO-EXTRACTED INTO dristi-common BY scripts/migration/dristi_common/03_build_canonical.py
// Source: dristi-services/ab-diary/src/main/java/digit/util/UrlShortenerUtil.java
// NOTE: imports referencing service-internal classes (ServiceConstants,
// CommonConfiguration, web.models.*) may need follow-up — see Phase 4.
package org.pucar.dristi.common.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import org.pucar.dristi.common.config.CommonConfiguration;
import static org.pucar.dristi.common.config.CommonConstants.*;

@Slf4j
@Component
public class UrlShortenerUtil {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private CommonConfiguration configs;


    public String getShortenedUrl(String url){

        HashMap<String,String> body = new HashMap<>();
        body.put(URL,url);
        StringBuilder builder = new StringBuilder(configs.getUrlShortnerHost());
        builder.append(configs.getUrlShortnerEndpoint());
        String res = restTemplate.postForObject(builder.toString(), body, String.class);

        if(StringUtils.isEmpty(res)){
            log.error(URL_SHORTENING_ERROR_CODE, URL_SHORTENING_ERROR_MESSAGE + url); ;
            return url;
        }
        else return res;
    }


}