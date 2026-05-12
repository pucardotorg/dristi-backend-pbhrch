package org.pucar.dristi.identityaccess.advocateoffice.internal.enrichment;

import com.fasterxml.jackson.databind.JsonNode;
import org.pucar.dristi.identityaccess.advocate.AdvocateApi;
import org.pucar.dristi.identityaccess.advocateoffice.internal.config.Configuration;
import org.pucar.dristi.common.contract.advocate.Advocate;
import org.pucar.dristi.common.contract.advocate.AdvocateClerk;
import org.pucar.dristi.common.util.IndividualUtil;
import org.pucar.dristi.common.contract.advocateoffice.AddMember;
import org.pucar.dristi.common.contract.advocateoffice.AddMemberRequest;
import org.pucar.dristi.common.contract.advocateoffice.LeaveOffice;
import org.pucar.dristi.common.contract.advocateoffice.LeaveOfficeRequest;
import org.pucar.dristi.common.contract.advocateoffice.UpdateMemberAccessRequest;
import org.pucar.dristi.common.contract.advocateoffice.MemberType;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.models.AuditDetails;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.User;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

import static org.pucar.dristi.identityaccess.advocateoffice.internal.config.ServiceConstants.*;

@Component
@Slf4j
public class AdvocateOfficeEnrichment {

    private final AdvocateApi advocateApi;
    private final IndividualUtil individualUtil;
    private final Configuration configuration;

    @Autowired
    public AdvocateOfficeEnrichment(AdvocateApi advocateApi,
                                    @Qualifier("commonIndividualUtil") IndividualUtil individualUtil,
                                    Configuration configuration) {
        this.advocateApi = advocateApi;
        this.individualUtil = individualUtil;
        this.configuration = configuration;
    }

    private String getIndividualIdFromAdvocateId(RequestInfo requestInfo, String advocateId) {
        List<Advocate> advocates = advocateApi.searchAdvocatesById(requestInfo, advocateId);
        if (advocates.isEmpty()) {
            throw new CustomException(ADVOCATE_NOT_FOUND,
                    String.format("Advocate not found for advocate id %s", advocateId));
        }
        return advocates.get(0).getIndividualId();
    }

    private String getUserUuidFromIndividualId(RequestInfo requestInfo, String tenantId, String individualId) {
        StringBuilder uri = new StringBuilder(configuration.getIndividualHost())
                .append(configuration.getIndividualSearchEndPoint())
                .append("?limit=1&offset=0&tenantId=").append(tenantId);

        java.util.Map<String, Object> individual = new java.util.HashMap<>();
        individual.put("individualId", individualId);
        java.util.Map<String, Object> request = new java.util.HashMap<>();
        request.put("RequestInfo", requestInfo);
        request.put("Individual", individual);

        JsonNode node = individualUtil.getIndividual(request, uri);
        if (node == null || node.isMissingNode() || node.isEmpty()) {
            throw new CustomException(INDIVIDUAL_NOT_FOUND,
                    String.format("Individual not found for individual id %s", individualId));
        }
        JsonNode userUuidNode = node.path("userUuid");
        if (userUuidNode.isMissingNode() || userUuidNode.isNull() || userUuidNode.asText().isBlank()) {
            return null;
        }
        return userUuidNode.asText();
    }

    private String getIndividualIdFromClerkId(RequestInfo requestInfo, String tenantId, String clerkId) {
        List<AdvocateClerk> clerks = advocateApi.searchClerksById(requestInfo, tenantId, clerkId);
        if (clerks.isEmpty()) {
            throw new CustomException(ADVOCATE_CLERK_NOT_FOUND,
                    String.format("Advocate clerk not found for clerk id %s", clerkId));
        }
        return clerks.get(0).getIndividualId();
    }

    public void enrichAddMemberRequest(AddMemberRequest request) {
        AddMember addMember = request.getAddMember();
        RequestInfo requestInfo = request.getRequestInfo();

        addMember.setId(UUID.randomUUID());
        addMember.setAuditDetails(getAuditDetailsForCreate(requestInfo));
        addMember.setIsActive(true);
        addMember.setAdvocateOfficeMobileNumber(requestInfo.getUserInfo().getMobileNumber());

        enrichOfficeAdvocateUserUuid(request);
        enrichMemberUserUuid(request);

        log.info("Enriched add member request with id: {}", addMember.getId());
    }

    private void enrichOfficeAdvocateUserUuid(AddMemberRequest request){
        AddMember addMember = request.getAddMember();
        RequestInfo requestInfo = request.getRequestInfo();
        String tenantId = addMember.getTenantId();
        String advocateId = addMember.getOfficeAdvocateId().toString();

        String advocateIndividualId = getIndividualIdFromAdvocateId(requestInfo, advocateId);
        String advocateUserUuid = getUserUuidFromIndividualId(requestInfo, tenantId, advocateIndividualId);
        addMember.setOfficeAdvocateUserUuid(UUID.fromString(advocateUserUuid));
        log.info("Enriched officeAdvocateUserUuid: {} for officeAdvocateId: {}", advocateUserUuid, addMember.getOfficeAdvocateId());
    }

    private void enrichMemberUserUuid(AddMemberRequest request){
        AddMember addMember = request.getAddMember();
        RequestInfo requestInfo = request.getRequestInfo();
        String tenantId = addMember.getTenantId();
        String memberId = addMember.getMemberId().toString();

        String memberIndividualId;
        if (addMember.getMemberType() == MemberType.ADVOCATE) {
            memberIndividualId = getIndividualIdFromAdvocateId(requestInfo, memberId);
        } else {
            memberIndividualId = getIndividualIdFromClerkId(requestInfo, tenantId, memberId);
        }

        String memberUserUuid = getUserUuidFromIndividualId(requestInfo, tenantId, memberIndividualId);
        addMember.setMemberUserUuid(UUID.fromString(memberUserUuid));
        log.info("Enriched memberUserUuid: {} for memberId: {}", memberUserUuid, addMember.getMemberId());
    }



    public void enrichLeaveOfficeRequest(LeaveOfficeRequest request) {
        LeaveOffice leaveOffice = request.getLeaveOffice();

        leaveOffice.setIsActive(false);

        log.info("Enriched leave office request with id: {}", leaveOffice.getId());
    }

    public void enrichUpdateMemberAccessRequest(UpdateMemberAccessRequest request, AddMember existingMember) {

        AuditDetails auditDetails = existingMember.getAuditDetails();
        auditDetails.setLastModifiedTime(System.currentTimeMillis());
        auditDetails.setLastModifiedBy(request.getRequestInfo().getUserInfo().getUuid());

        request.getUpdateMemberAccess().setAuditDetails(auditDetails);

        log.info("Enriched update member access request for member {} of advocate office : {}", existingMember.getMemberUserUuid(), existingMember.getOfficeAdvocateUserUuid());
    }

    private AuditDetails getAuditDetailsForCreate(RequestInfo requestInfo) {
        User user = requestInfo.getUserInfo();
        long currentTime = System.currentTimeMillis();

        return AuditDetails.builder()
                .createdBy(user.getUuid())
                .lastModifiedBy(user.getUuid())
                .createdTime(currentTime)
                .lastModifiedTime(currentTime)
                .build();
    }

}
