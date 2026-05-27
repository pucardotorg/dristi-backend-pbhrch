package org.pucar.dristi.integration.icops;

import org.egov.common.contract.request.RequestInfo;
import org.pucar.dristi.common.contract.icops.Location;
import org.pucar.dristi.common.contract.icops.LocationBasedJurisdiction;

/**
 * Public, cross-subdomain API of the icops subdomain. Other modules
 * (summons today) consume icops through this interface — never by
 * importing from {@code internal/}.
 */
public interface IcopsApi {

    /**
     * Returns the police-station jurisdiction for the given geo coordinate,
     * delegating to the Kerala iCops integration.
     *
     * @param requestInfo eGov request envelope carrying caller identity
     * @param location    latitude/longitude to resolve jurisdiction for
     */
    LocationBasedJurisdiction getLocationBasedJurisdiction(RequestInfo requestInfo, Location location);
}
