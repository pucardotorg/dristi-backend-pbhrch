// HAND-CURATED — do not regenerate
package org.pucar.dristi.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Spring properties needed by classes living in dristi-common.
 *
 * <p>This is the minimum surface required by the canonical utilities (IdgenUtil,
 * FileStoreUtil, MdmsUtil, UrlShortenerUtil, UserUtil, WorkflowUtil) — it is
 * <em>not</em> a replacement for any service's full Configuration class.
 * Property keys mirror the legacy DRISTI service Configuration so that
 * existing application.yml / application.properties files continue to work
 * without changes.
 *
 * <p>Getters/setters are written by hand (not Lombok) so this class never
 * depends on annotation-processor wiring being correct.
 */
@Component("commonConfiguration")
public class CommonConfiguration {

    @Value("${egov.idgen.host}")
    private String idGenHost;

    @Value("${egov.idgen.path}")
    private String idGenPath;

    @Value("${egov.filestore.host}")
    private String fileStoreHost;

    @Value("${egov.filestore.path}")
    private String fileStorePath;

    @Value("${egov.file.store.delete.endpoint:#{null}}")
    private String fileStoreDeleteEndPoint;

    @Value("${egov.mdms.host}")
    private String mdmsHost;

    @Value("${egov.mdms.search.endpoint}")
    private String mdmsEndPoint;

    @Value("${egov.url.shortner.host:#{null}}")
    private String urlShortnerHost;

    @Value("${egov.url.shortner.endpoint:#{null}}")
    private String urlShortnerEndpoint;

    @Value("${egov.user.host}")
    private String userHost;

    @Value("${egov.user.create.path:#{null}}")
    private String userCreateEndpoint;

    @Value("${egov.user.search.path}")
    private String userSearchEndpoint;

    @Value("${egov.user.update.path:#{null}}")
    private String userUpdateEndpoint;

    @Value("${egov.workflow.host}")
    private String wfHost;

    @Value("${egov.workflow.transition.path:#{null}}")
    private String wfTransitionPath;

    @Value("${egov.workflow.businessservice.search.path:#{null}}")
    private String wfBusinessServiceSearchPath;

    @Value("${egov.individual.host:#{null}}")
    private String individualHost;

    @Value("${egov.individual.search.path:#{null}}")
    private String individualSearchEndpoint;

    public String getIdGenHost() { return idGenHost; }
    public String getIdGenPath() { return idGenPath; }
    public String getFileStoreHost() { return fileStoreHost; }
    public String getFileStorePath() { return fileStorePath; }
    public String getFileStoreDeleteEndPoint() { return fileStoreDeleteEndPoint; }
    public String getMdmsHost() { return mdmsHost; }
    public String getMdmsEndPoint() { return mdmsEndPoint; }
    public String getUrlShortnerHost() { return urlShortnerHost; }
    public String getUrlShortnerEndpoint() { return urlShortnerEndpoint; }
    public String getUserHost() { return userHost; }
    public String getUserCreateEndpoint() { return userCreateEndpoint; }
    public String getUserSearchEndpoint() { return userSearchEndpoint; }
    public String getUserUpdateEndpoint() { return userUpdateEndpoint; }
    public String getWfHost() { return wfHost; }
    public String getWfTransitionPath() { return wfTransitionPath; }
    public String getWfBusinessServiceSearchPath() { return wfBusinessServiceSearchPath; }
    public String getIndividualHost() { return individualHost; }
    public String getIndividualSearchEndpoint() { return individualSearchEndpoint; }
}
