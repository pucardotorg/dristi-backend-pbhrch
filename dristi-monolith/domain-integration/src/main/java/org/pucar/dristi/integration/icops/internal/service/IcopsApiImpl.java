package org.pucar.dristi.integration.icops.internal.service;

import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.pucar.dristi.common.contract.icops.Location;
import org.pucar.dristi.common.contract.icops.LocationBasedJurisdiction;
import org.pucar.dristi.integration.icops.IcopsApi;
import org.pucar.dristi.integration.icops.internal.model.LocationRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class IcopsApiImpl implements IcopsApi {

    private final IcopsService icopsService;

    @Autowired
    public IcopsApiImpl(IcopsService icopsService) {
        this.icopsService = icopsService;
    }

    @Override
    public LocationBasedJurisdiction getLocationBasedJurisdiction(RequestInfo requestInfo, Location location) {
        LocationRequest request = LocationRequest.builder()
                .requestInfo(requestInfo)
                .location(location)
                .build();
        try {
            return icopsService.getLocationBasedJurisdiction(request);
        } catch (Exception e) {
            throw new CustomException("ICOPS_LOCATION_JURISDICTION_ERROR",
                    "Error occurred when getting location based jurisdiction from iCops");
        }
    }
}
