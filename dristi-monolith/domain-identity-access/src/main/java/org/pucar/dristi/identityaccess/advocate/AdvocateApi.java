package org.pucar.dristi.identityaccess.advocate;

import org.egov.common.contract.request.RequestInfo;
import org.pucar.dristi.common.contract.advocate.Advocate;

import java.util.List;
import java.util.Set;

/**
 * Public, cross-subdomain API of the advocate subdomain. Other modules
 * (cases today) consume advocate through this interface — never by importing
 * from {@code internal/}.
 */
public interface AdvocateApi {

    /**
     * Returns active advocates with the given registration ID.
     *
     * @param requestInfo eGov request envelope carrying caller identity
     * @param advocateId  advocate registration UUID
     */
    List<Advocate> searchAdvocatesById(RequestInfo requestInfo, String advocateId);

    /**
     * Returns active advocates whose {@code individualId} matches the given value.
     *
     * @param requestInfo  eGov request envelope carrying caller identity
     * @param individualId individual profile identifier
     */
    List<Advocate> searchAdvocatesByIndividualId(RequestInfo requestInfo, String individualId);

    /**
     * Returns {@code true} if any active advocate exists with the given registration ID.
     *
     * @param requestInfo eGov request envelope carrying caller identity
     * @param advocateId  advocate registration UUID
     */
    boolean advocateExists(RequestInfo requestInfo, String advocateId);

    /**
     * Returns the set of {@code individualId} values for all active advocates
     * whose registration IDs are in {@code advocateIds}.
     *
     * @param requestInfo eGov request envelope carrying caller identity
     * @param advocateIds list of advocate registration UUIDs to look up
     */
    Set<String> getAdvocateIndividualIds(RequestInfo requestInfo, List<String> advocateIds);
}
