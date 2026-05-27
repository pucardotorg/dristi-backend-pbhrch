package org.pucar.dristi.integration.esign.internal.service;

import lombok.RequiredArgsConstructor;
import org.pucar.dristi.common.contract.esign.Coordinate;
import org.pucar.dristi.common.contract.esign.CoordinateRequest;
import org.pucar.dristi.integration.esign.EsignApi;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("esignApiImpl")
@RequiredArgsConstructor
public class EsignApiImpl implements EsignApi {

    private final ESignService eSignService;

    @Override
    public List<Coordinate> getLocationForSign(CoordinateRequest request) {
        return eSignService.getLocation(request);
    }
}
