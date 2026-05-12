package org.pucar.dristi.identityaccess.advocateoffice;

import org.egov.common.contract.request.RequestInfo;
import org.pucar.dristi.common.contract.advocateoffice.AddMember;
import org.pucar.dristi.common.contract.advocateoffice.MemberSearchCriteria;

import java.util.List;
import java.util.UUID;

/**
 * Public, cross-subdomain API of the advocateoffice subdomain. Other modules
 * consume advocate-office through this interface — never by importing from {@code internal/}.
 */
public interface AdvocateOfficeApi {

    /**
     * Returns active members matching the given search criteria.
     *
     * @param requestInfo eGov request envelope carrying caller identity
     * @param criteria    search filters (tenantId, officeAdvocateId, memberUserUuid, isActive, …)
     */
    List<AddMember> searchMembers(RequestInfo requestInfo, MemberSearchCriteria criteria);

    /**
     * Returns {@code true} if the given user is an active member of the advocate's office
     * and has {@code allowCaseCreate} set.
     *
     * @param requestInfo      eGov request envelope
     * @param tenantId         tenant identifier
     * @param officeAdvocateId UUID of the office advocate
     * @param memberUserUuid   UUID of the user to check
     */
    boolean isUserMemberOfAdvocateOffice(RequestInfo requestInfo, String tenantId,
                                         UUID officeAdvocateId, UUID memberUserUuid);
}
