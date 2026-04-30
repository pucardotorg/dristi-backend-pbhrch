// HAND-CURATED — imports rewired to dristi-common; do not regenerate
// AUTO-EXTRACTED INTO dristi-common BY scripts/migration/dristi_common/03_build_canonical.py
// Source: dristi-services/hearing/src/main/java/org/pucar/dristi/util/FileStoreUtil.java
// NOTE: imports referencing service-internal classes (ServiceConstants,
// CommonConfiguration, web.models.*) may need follow-up — see Phase 4.
package org.pucar.dristi.common.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.tracer.model.CustomException;
import org.pucar.dristi.common.config.CommonConfiguration;
import org.pucar.dristi.common.models.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

import static org.pucar.dristi.common.config.CommonConstants.FILE_STORE_UTILITY_EXCEPTION;


@Component("commonFileStoreUtil")
@Slf4j
public class FileStoreUtil {

    private final CommonConfiguration configs;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    public FileStoreUtil(RestTemplate restTemplate, CommonConfiguration configs, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.configs = configs;
        this.objectMapper = objectMapper;
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
    /**
     * Uploads raw bytes to filestore as a multipart `file` field, returning
     * the parsed {@link Document} (id / fileName / fileStore are populated
     * from the response).
     *
     * @param payInSlipBytes raw bytes to upload (PDF, image, etc.)
     * @param tenantId       tenant for the upload URI
     */
    public Document saveDocumentToFileStore(byte[] payInSlipBytes, String tenantId) {
        try {
            String uri = configs.getFileStoreHost() + configs.getFileStorePath()
                    + "?tenantId=" + tenantId;

            ByteArrayResource byteArrayResource = new ByteArrayResource(payInSlipBytes) {
                @Override
                public String getFilename() {
                    return "file.pdf";
                }
            };

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", byteArrayResource);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            ResponseEntity<Object> responseEntity = restTemplate.postForEntity(uri, requestEntity, Object.class);
            return extractDocumentFromResponse(responseEntity);
        } catch (Exception e) {
            log.error("Error while saving document to file store: {}", e.getMessage(), e);
            throw new CustomException(FILE_STORE_UTILITY_EXCEPTION,
                    "Error occurred when saving document in File Store");
        }
    }

    /**
     * Best-effort extraction of the first File entry from the filestore
     * upload response into a {@link Document}.
     */
    @SuppressWarnings("unchecked")
    private Document extractDocumentFromResponse(ResponseEntity<Object> response) {
        if (response == null || response.getBody() == null) {
            return Document.builder().build();
        }
        Map<String, Object> body = objectMapper.convertValue(response.getBody(), Map.class);
        Object filesNode = body.get("files");
        if (filesNode instanceof List<?> files && !files.isEmpty()) {
            Map<String, Object> first = objectMapper.convertValue(files.get(0), Map.class);
            return Document.builder()
                    .id((String) first.get("id"))
                    .fileStore((String) first.get("fileStoreId"))
                    .fileName((String) first.get("fileName"))
                    .build();
        }
        return Document.builder().build();
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
