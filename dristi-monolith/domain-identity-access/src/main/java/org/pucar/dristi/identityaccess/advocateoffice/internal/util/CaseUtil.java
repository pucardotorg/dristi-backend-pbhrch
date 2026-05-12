package org.pucar.dristi.identityaccess.advocateoffice.internal.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.pucar.dristi.identityaccess.advocateoffice.internal.config.Configuration;
import org.pucar.dristi.common.repository.ServiceRequestRepository;
import org.pucar.dristi.common.contract.advocateoffice.CaseMemberSearchRequest;
import org.pucar.dristi.common.contract.advocateoffice.CaseMemberSearchResponse;
import org.pucar.dristi.common.contract.advocateoffice.ProcessCaseMemberRequest;
import lombok.extern.slf4j.Slf4j;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

// CIRCULAR-DEP BLOCKER: Converting these REST calls to direct CaseApi calls requires
// domain-identity-access to depend on domain-case-lifecycle. That creates a compile-time
// cycle because domain-case-lifecycle already depends on domain-identity-access for
// AdvocateOfficeApi. The target methods in domain-case-lifecycle are:
//   AdvocateOfficeCaseMemberService.searchCaseMembers()  → /case/v1/_searchCaseMember
//   AdvocateOfficeCaseMemberService.processCaseMember()  → /case/v1/_processCaseMember
// Conversion is deferred until the bidirectional dependency is resolved.
@Component
@Slf4j
public class CaseUtil {

    private final Configuration configuration;
    private final ServiceRequestRepository serviceRequestRepository;
    private final ObjectMapper objectMapper;

    @Autowired
    public CaseUtil(Configuration configuration,
                    ServiceRequestRepository serviceRequestRepository,
                    ObjectMapper objectMapper) {
        this.configuration = configuration;
        this.serviceRequestRepository = serviceRequestRepository;
        this.objectMapper = objectMapper;
    }

    public CaseMemberSearchResponse searchCaseMembers(CaseMemberSearchRequest request) {
        StringBuilder uri = new StringBuilder(configuration.getCaseHost())
                .append(configuration.getCaseMemberSearchEndPoint());

        Object response = serviceRequestRepository.fetchResult(uri, request);

        if (response == null) {
            log.error("No response received from case service for case member search");
            throw new CustomException("CASE_MEMBER_SEARCH_ERROR", "Unable to fetch case member information");
        }

        CaseMemberSearchResponse caseMemberSearchResponse = objectMapper.convertValue(response, CaseMemberSearchResponse.class);
        if (caseMemberSearchResponse.getPagination() == null) {
            caseMemberSearchResponse.setPagination(request.getPagination());
        }

        Integer totalCount = caseMemberSearchResponse.getTotalCount() != null
                ? caseMemberSearchResponse.getTotalCount()
                : (caseMemberSearchResponse.getCases() == null ? 0 : caseMemberSearchResponse.getCases().size());
        caseMemberSearchResponse.setTotalCount(totalCount);

        return caseMemberSearchResponse;
    }

    public void processCaseMember(ProcessCaseMemberRequest request) {
        StringBuilder uri = new StringBuilder(configuration.getCaseHost())
                .append(configuration.getProcessCaseMemberEndPoint());

        Object response = serviceRequestRepository.fetchResult(uri, request);

        if (response == null) {
            log.error("No response received from case service for process case member");
            throw new CustomException("PROCESS_CASE_MEMBER_ERROR", "Unable to process case member");
        }
    }
}
