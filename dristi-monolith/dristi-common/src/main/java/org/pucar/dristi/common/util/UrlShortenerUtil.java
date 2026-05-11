// HAND-CURATED — imports rewired to dristi-common; do not regenerate
// AUTO-EXTRACTED INTO dristi-common BY scripts/migration/dristi_common/03_build_canonical.py
// Source: dristi-services/ab-diary/src/main/java/digit/util/UrlShortenerUtil.java
// NOTE: imports referencing service-internal classes (ServiceConstants,
// CommonConfiguration, web.models.*) may need follow-up — see Phase 4.
package org.pucar.dristi.common.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import org.pucar.dristi.common.config.CommonConfiguration;
import static org.pucar.dristi.common.config.CommonConstants.*;

@Slf4j
@Component("commonUrlShortenerUtil")
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

    /**
     * Variant of {@link #getShortenedUrl(String)} that also tags the
     * shortened entry with a {@code referenceId} (so it can be expired
     * later by reference). Used by services that need recoverable /
     * expirable short links (e.g. digitalized-documents signing flows).
     */
    public String getShortenedUrl(String url, String referenceId) {
        HashMap<String, String> body = new HashMap<>();
        body.put(URL, url);
        body.put(REFERENCE_ID, referenceId);
        StringBuilder builder = new StringBuilder(configs.getUrlShortnerHost());
        builder.append(configs.getUrlShortnerEndpoint());

        try {
            String res = restTemplate.postForObject(builder.toString(), body, String.class);
            if (StringUtils.isEmpty(res)) {
                log.error(URL_SHORTENING_ERROR_MESSAGE);
                return url;
            }
            return res;
        } catch (Exception e) {
            log.error("Error occurred while calling url shortening service: {}", e.getMessage());
            return url;
        }
    }

    /**
     * Builds the canonical long-URL from {@code domain.url} + {@code egov.base.url}
     * (or an explicit {@code basePath}) + {@code egov.long.url} format and
     * returns its shortened form. {@code documentNumber} is also threaded
     * through as the shortener referenceId for later expiry.
     */
    public String createShortenedUrl(String tenantId, String documentNumber, String type) {
        return createShortenedUrl(tenantId, documentNumber, type, configs.getBaseUrl());
    }

    public String createShortenedUrl(String tenantId, String documentNumber, String type, String basePath) {
        try {
            String baseUrl = configs.getDomainUrl() + basePath;
            String longUrl = String.format(configs.getLongUrl(), baseUrl, tenantId, documentNumber, type);
            return getShortenedUrl(longUrl, documentNumber);
        } catch (CustomException e) {
            log.error(URL_SHORTENING_ERROR_CODE + "{}", e.getMessage());
            throw new CustomException(URL_SHORTENING_ERROR_CODE, URL_SHORTENING_ERROR_MESSAGE + e.getMessage());
        }
    }

    /**
     * Marks the given short url as expired, keyed by {@code referenceId}.
     * Generic over the caller's DTO shape — caller passes the already-extracted
     * primitives so this stays free of service-specific request types.
     */
    public void expireTheUrl(String url, String referenceId) {
        HashMap<String, String> body = new HashMap<>();
        body.put(URL, url);
        body.put(REFERENCE_ID, referenceId);
        StringBuilder builder = new StringBuilder(configs.getUrlShortnerHost());
        builder.append(configs.getUrlShortenerExpireEndpoint());
        restTemplate.postForObject(builder.toString(), body, String.class);
    }
}