package org.pucar.dristi.caselifecycle.ordermanagement.internal.util;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.pucar.dristi.integration.esign.EsignApi;
import org.pucar.dristi.common.contract.ordermanagement.Coordinate;
import org.pucar.dristi.common.contract.ordermanagement.CoordinateRequest;

import java.util.Collections;
import java.util.List;

import static org.pucar.dristi.caselifecycle.ordermanagement.internal.config.ServiceConstants.ESIGN_SERVICE_EXCEPTION;

@Component("ordermanagementESignUtil")
@Slf4j
public class ESignUtil {

    private final EsignApi esignApi;
    private final ObjectMapper mapper;

    @Autowired
    public ESignUtil(EsignApi esignApi, ObjectMapper mapper) {
        this.esignApi = esignApi;
        this.mapper = mapper;
    }


    public List<Coordinate> getCoordinateForSign(CoordinateRequest request) {
        try {
            org.pucar.dristi.common.contract.esign.CoordinateRequest bridgedRequest =
                    mapper.convertValue(request,
                            org.pucar.dristi.common.contract.esign.CoordinateRequest.class);
            List<org.pucar.dristi.common.contract.esign.Coordinate> apiResult =
                    esignApi.getLocationForSign(bridgedRequest);
            if (apiResult == null) {
                return Collections.emptyList();
            }
            return apiResult.stream()
                    .map(c -> mapper.convertValue(c, Coordinate.class))
                    .toList();
        } catch (Exception e) {
            throw new CustomException(ESIGN_SERVICE_EXCEPTION, "Error occurred while getting coordinates");
        }

    }
}
