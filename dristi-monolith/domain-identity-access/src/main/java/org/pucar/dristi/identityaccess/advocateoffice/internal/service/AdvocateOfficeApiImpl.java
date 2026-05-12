package org.pucar.dristi.identityaccess.advocateoffice.internal.service;

import org.egov.common.contract.request.RequestInfo;
import org.pucar.dristi.common.contract.advocateoffice.AddMember;
import org.pucar.dristi.common.contract.advocateoffice.MemberSearchCriteria;
import org.pucar.dristi.common.contract.advocateoffice.MemberSearchRequest;
import org.pucar.dristi.identityaccess.advocateoffice.AdvocateOfficeApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class AdvocateOfficeApiImpl implements AdvocateOfficeApi {

    private final AdvocateOfficeService advocateOfficeService;

    @Autowired
    public AdvocateOfficeApiImpl(AdvocateOfficeService advocateOfficeService) {
        this.advocateOfficeService = advocateOfficeService;
    }

    @Override
    public List<AddMember> searchMembers(RequestInfo requestInfo, MemberSearchCriteria criteria) {
        MemberSearchRequest request = MemberSearchRequest.builder()
                .requestInfo(requestInfo)
                .searchCriteria(criteria)
                .build();
        return advocateOfficeService.searchMembers(request);
    }

    @Override
    public boolean isUserMemberOfAdvocateOffice(RequestInfo requestInfo, String tenantId,
                                                UUID officeAdvocateId, UUID memberUserUuid) {
        MemberSearchCriteria criteria = MemberSearchCriteria.builder()
                .tenantId(tenantId)
                .officeAdvocateId(officeAdvocateId)
                .memberUserUuid(memberUserUuid)
                .isActive(true)
                .build();
        List<AddMember> members = searchMembers(requestInfo, criteria);
        return !members.isEmpty() && Boolean.TRUE.equals(members.get(0).getAllowCaseCreate());
    }
}
