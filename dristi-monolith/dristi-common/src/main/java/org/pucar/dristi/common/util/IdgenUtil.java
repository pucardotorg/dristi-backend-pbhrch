// HAND-CURATED — do not regenerate
// Refactored to merge advocate/hearing/evidence/task style: constructor
// injection, structured logging, isolated fetchIdGenerationResponse helper,
// using DRISTI's IdRequest (with isSequencePadded) extracted to dristi-common.
package org.pucar.dristi.common.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.pucar.dristi.common.config.CommonConfiguration;
import org.pucar.dristi.common.models.idgen.IdGenerationRequest;
import org.pucar.dristi.common.models.idgen.IdGenerationResponse;
import org.pucar.dristi.common.models.idgen.IdRequest;
import org.pucar.dristi.common.models.idgen.IdResponse;
import org.pucar.dristi.common.repository.ServiceRequestRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

import static org.pucar.dristi.common.config.CommonConstants.IDGEN_ERROR;
import static org.pucar.dristi.common.config.CommonConstants.NO_IDS_FOUND_ERROR;

@Component("commonIdgenUtil")
public class IdgenUtil {

    private static final Logger log = LoggerFactory.getLogger(IdgenUtil.class);

    private final ObjectMapper mapper;
    private final ServiceRequestRepository restRepo;
    private final CommonConfiguration configs;

    @Autowired
    public IdgenUtil(ObjectMapper mapper, ServiceRequestRepository restRepo, CommonConfiguration configs) {
        this.mapper = mapper;
        this.restRepo = restRepo;
        this.configs = configs;
    }

    public List<String> getIdList(
            RequestInfo requestInfo,
            String tenantId,
            String idName,
            String idformat,
            Integer count,
            Boolean isSequencePadded) {
        try {
            List<IdRequest> reqList = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                reqList.add(IdRequest.builder()
                        .idName(idName)
                        .isSequencePadded(isSequencePadded)
                        .format(idformat)
                        .tenantId(tenantId)
                        .build());
            }

            IdGenerationRequest request = IdGenerationRequest.builder()
                    .idRequests(reqList)
                    .requestInfo(requestInfo)
                    .build();

            StringBuilder uri = new StringBuilder(configs.getIdGenHost())
                    .append(configs.getIdGenPath());

            IdGenerationResponse response = fetchIdGenerationResponse(uri, request);
            List<IdResponse> idResponses = response.getIdResponses();

            if (CollectionUtils.isEmpty(idResponses)) {
                throw new CustomException(IDGEN_ERROR, NO_IDS_FOUND_ERROR);
            }

            return List.copyOf(idResponses.stream().map(IdResponse::getId).toList());
        } catch (CustomException e) {
            log.error("Custom Exception occurred in calling Idgen :: {}", e.toString());
            throw e;
        } catch (Exception e) {
            throw new CustomException(IDGEN_ERROR, "ERROR in IDGEN Service");
        }
    }

    private IdGenerationResponse fetchIdGenerationResponse(StringBuilder uri, IdGenerationRequest request) {
        try {
            return mapper.convertValue(restRepo.fetchResult(uri, request), IdGenerationResponse.class);
        } catch (CustomException e) {
            log.error("Custom Exception occurred in Idgen Utility :: {}", e.toString());
            throw e;
        } catch (Exception e) {
            log.error("Error fetching ID from ID generation service :: {}", e.toString());
            throw new CustomException(IDGEN_ERROR, "Error fetching ID from ID generation service");
        }
    }
}
