// HAND-CURATED — imports rewired to dristi-common; do not regenerate
// AUTO-EXTRACTED INTO dristi-common BY scripts/migration/dristi_common/03_build_canonical.py
// Source: dristi-services/hearing/src/main/java/org/pucar/dristi/util/FileStoreUtil.java
// NOTE: imports referencing service-internal classes (ServiceConstants,
// CommonConfiguration, web.models.*) may need follow-up — see Phase 4.
package org.pucar.dristi.common.util;

import lombok.extern.slf4j.Slf4j;
import org.egov.tracer.model.CustomException;
import org.pucar.dristi.common.config.CommonConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.List;


@Component
@Slf4j
public class FileStoreUtil {

    private CommonConfiguration configs;

    private RestTemplate restTemplate;

    @Autowired
    public FileStoreUtil(RestTemplate restTemplate, CommonConfiguration configs) {
        this.restTemplate = restTemplate;
        this.configs = configs;
    }

    /**
     * Returns whether the file exists or not in the filestore.
     * @param tenantId
     * @param fileStoreId
     * @return
     */
    public boolean doesFileExist(String tenantId,  String fileStoreId) {
    		boolean fileExists = false;
        try{
            StringBuilder uri = new StringBuilder(configs.getFileStoreHost()).append(configs.getFileStorePath());
            uri.append("tenantId=").append(tenantId).append("&").append("fileStoreId=").append(fileStoreId);
            ResponseEntity<String> responseEntity= restTemplate.getForEntity(uri.toString(), String.class);
            fileExists = responseEntity.getStatusCode().equals(HttpStatus.OK);
        }catch (Exception e){
        		log.error("Document {} is not found in the Filestore for tenantId {} ! An exception occurred!", 
        			  fileStoreId, 
        			  tenantId, 
        			  e);
        }
        return fileExists;
    }
    public void deleteFilesByFileStore(List<String> fileStoreIds, String tenantId) {
        if (fileStoreIds == null || fileStoreIds.isEmpty()) {
            log.warn("No file store IDs provided for deletion");
            return;
        }
        String url = configs.getFileStoreHost() + configs.getFileStoreDeleteEndPoint() + "?tenantId=" + tenantId;

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("fileStoreIds", String.join(",", fileStoreIds));
        body.add("isSoftDelete", false);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, new HttpHeaders());
        Object response = null;
        try {
            ResponseEntity<Object> responseEntity = restTemplate.postForEntity(url, requestEntity, Object.class);
            log.info("Files deleted from filestore: {}, status: {}", fileStoreIds, responseEntity.getStatusCode());
        } catch (CustomException e) {
            log.error("Error while deleting files from file store: {}", e.getMessage(), e);
            throw new CustomException("FILE_STORE_UTILITY_EXCEPTION", "Error occurred when deleting files in File Store");
        }
    }
}
