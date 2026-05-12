// HAND-CURATED — imports rewired to dristi-common; do not regenerate
// AUTO-EXTRACTED INTO dristi-common BY scripts/migration/dristi_common/03_build_canonical.py
// Source: dristi-services/hearing/src/main/java/org/pucar/dristi/util/FileStoreUtil.java
// NOTE: imports referencing service-internal classes (ServiceConstants,
// CommonConfiguration, web.models.*) may need follow-up — see Phase 4.
package org.pucar.dristi.common.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.tracer.model.CustomException;
import org.json.JSONObject;
import org.pucar.dristi.common.config.CommonConfiguration;
import org.pucar.dristi.common.models.Document;
import org.pucar.dristi.common.repository.ServiceRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

import static org.pucar.dristi.common.config.CommonConstants.FILE;
import static org.pucar.dristi.common.config.CommonConstants.FILE_STORE_SERVICE_EXCEPTION_CODE;
import static org.pucar.dristi.common.config.CommonConstants.FILE_STORE_UTILITY_EXCEPTION;
import static org.pucar.dristi.common.config.CommonConstants.INVALID_INPUT;


@Component("commonFileStoreUtil")
@Slf4j
public class FileStoreUtil {

    private final CommonConfiguration configs;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final ServiceRequestRepository serviceRequestRepository;

    @Autowired
    public FileStoreUtil(RestTemplate restTemplate, CommonConfiguration configs,
                         ObjectMapper objectMapper,
                         ServiceRequestRepository serviceRequestRepository) {
        this.restTemplate = restTemplate;
        this.configs = configs;
        this.objectMapper = objectMapper;
        this.serviceRequestRepository = serviceRequestRepository;
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
     * Fetches raw bytes for a file from filestore by tenantId + fileStoreId.
     */
    public byte[] getFile(String tenantId, String fileStoreId) {
        try {
            StringBuilder uri = new StringBuilder(configs.getFileStoreHost()).append(configs.getFileStorePath());
            uri.append("tenantId=").append(tenantId).append("&").append("fileStoreId=").append(fileStoreId);
            ResponseEntity<Resource> responseEntity = restTemplate.getForEntity(uri.toString(), Resource.class);
            return responseEntity.getBody().getContentAsByteArray();
        } catch (Exception e) {
            log.error("Document {} is not found in the Filestore for tenantId {} ! An exception occurred!",
                    fileStoreId, tenantId, e);
        }
        return null;
    }

    /**
     * Best-effort extraction of the first File entry from the filestore
     * upload response into a {@link Document}.
     */
    @SuppressWarnings("unchecked")
    public Document extractDocumentFromResponse(ResponseEntity<Object> response) {
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

    /**
     * Appends two query parameters to {@code uri}, prefixing with {@code ?} or
     * {@code &} depending on whether the URI already contains a query string.
     * Service-agnostic URI builder helper.
     */
    public StringBuilder appendQueryParams(StringBuilder uri, String paramName1, String paramValue1,
                                           String paramName2, String paramValue2) {
        if (uri.indexOf("?") == -1) {
            uri.append("?");
        } else {
            uri.append("&");
        }
        uri.append(paramName1).append("=").append(paramValue1).append("&");
        uri.append(paramName2).append("=").append(paramValue2);
        return uri;
    }

    /**
     * Fetches a binary {@link Resource} body from filestore by id + tenant via
     * the {@code egov.filestore.search.endpoint}. Inputs are sanitized against
     * a conservative {@code [a-zA-Z0-9_-]+} pattern.
     */
    public Resource fetchFileStoreObjectById(String fileStoreId, String tenantId) {
        log.info("Fetching file store object by id: {}", fileStoreId);
        if (!isValidFileStoreId(fileStoreId) || !isValidTenantId(tenantId)) {
            throw new CustomException(INVALID_INPUT, "Invalid fileStoreId or tenantId");
        }
        StringBuilder uri = new StringBuilder();
        uri.append(configs.getFileStoreHost()).append(configs.getFileStoreSearchEndpoint());
        uri = appendQueryParams(uri, "fileStoreId", fileStoreId, "tenantId", tenantId);
        try {
            Resource object = serviceRequestRepository.fetchResultGetForResource(uri);
            log.info("Successfully fetched file store object by id: {}", fileStoreId);
            return object;
        } catch (Exception e) {
            throw new CustomException(FILE_STORE_SERVICE_EXCEPTION_CODE, e.getMessage());
        }
    }

    private static boolean isValidFileStoreId(String fileStoreId) {
        return fileStoreId != null && fileStoreId.matches("[a-zA-Z0-9_-]+");
    }

    private static boolean isValidTenantId(String tenantId) {
        return tenantId != null && tenantId.matches("[a-zA-Z0-9_-]+");
    }

    /**
     * Uploads {@code file} to filestore under the given {@code tenantId} and
     * {@code module} (caller-supplied — formerly a hardcoded service-local
     * string). Returns the {@code fileStoreId} of the first uploaded file in
     * the response.
     */
    public String storeFileInFileStore(MultipartFile file, String tenantId, String module) {
        if (!FileValidationUtil.isValidFile(file)) {
            throw new IllegalArgumentException("Invalid file type");
        }
        StringBuilder uri = new StringBuilder();
        uri.append(configs.getFileStoreHost()).append(configs.getFileStoreSaveEndPoint());

        MultiValueMap<String, Object> request = new LinkedMultiValueMap<>();
        request.add(FILE, file.getResource());
        request.add("tenantId", tenantId);
        request.add("module", module);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(request, headers);

        ResponseEntity<String> response = restTemplate.exchange(uri.toString(), HttpMethod.POST, entity, String.class);
        String body = response.getBody();
        JSONObject jsonObject = new JSONObject(body);
        JSONObject fileObject = jsonObject.getJSONArray("files").getJSONObject(0);
        return fileObject.getString("fileStoreId");
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
