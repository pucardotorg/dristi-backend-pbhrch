package org.pucar.dristi.identityaccess.advocate.internal.service.impl;

import org.egov.common.contract.request.RequestInfo;
import org.pucar.dristi.common.contract.advocate.Advocate;
import org.pucar.dristi.common.contract.advocate.AdvocateClerk;
import org.pucar.dristi.common.contract.advocate.AdvocateClerkSearchCriteria;
import org.pucar.dristi.common.contract.advocate.AdvocateSearchCriteria;
import org.pucar.dristi.identityaccess.advocate.AdvocateApi;
import org.pucar.dristi.identityaccess.advocate.internal.service.AdvocateClerkService;
import org.pucar.dristi.identityaccess.advocate.internal.service.AdvocateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class AdvocateApiImpl implements AdvocateApi {

    private final AdvocateService advocateService;
    private final AdvocateClerkService advocateClerkService;

    @Autowired
    public AdvocateApiImpl(AdvocateService advocateService, AdvocateClerkService advocateClerkService) {
        this.advocateService = advocateService;
        this.advocateClerkService = advocateClerkService;
    }

    @Override
    public List<Advocate> searchAdvocatesById(RequestInfo requestInfo, String advocateId) {
        return search(requestInfo, AdvocateSearchCriteria.builder().id(advocateId).build());
    }

    @Override
    public List<Advocate> searchAdvocatesByIndividualId(RequestInfo requestInfo, String individualId) {
        return search(requestInfo, AdvocateSearchCriteria.builder().individualId(individualId).build());
    }

    @Override
    public List<Advocate> searchAdvocatesByBarRegistrationNumber(RequestInfo requestInfo, String barRegistrationNumber) {
        return search(requestInfo, AdvocateSearchCriteria.builder().barRegistrationNumber(barRegistrationNumber).build());
    }

    @Override
    public boolean advocateExists(RequestInfo requestInfo, String advocateId) {
        return !searchAdvocatesById(requestInfo, advocateId).isEmpty();
    }

    @Override
    public Set<String> getAdvocateIndividualIds(RequestInfo requestInfo, List<String> advocateIds) {
        List<AdvocateSearchCriteria> criteriaList = advocateIds.stream()
                .map(id -> AdvocateSearchCriteria.builder().id(id).build())
                .collect(Collectors.toList());
        int limit = Math.max(advocateIds.size() * 2, 10);
        advocateService.searchAdvocate(requestInfo, criteriaList, null, limit, 0);
        return criteriaList.stream()
                .filter(c -> c.getResponseList() != null)
                .flatMap(c -> c.getResponseList().stream())
                .filter(Advocate::getIsActive)
                .map(Advocate::getIndividualId)
                .collect(Collectors.toSet());
    }

    @Override
    public List<AdvocateClerk> searchClerksById(RequestInfo requestInfo, String tenantId, String clerkId) {
        AdvocateClerkSearchCriteria criteria = AdvocateClerkSearchCriteria.builder()
                .id(clerkId).build();
        List<AdvocateClerkSearchCriteria> criteriaList = new ArrayList<>();
        criteriaList.add(criteria);
        advocateClerkService.searchAdvocateClerkApplications(requestInfo, criteriaList, tenantId, 10, 0);
        return criteria.getResponseList() == null
                ? List.of()
                : criteria.getResponseList().stream()
                    .filter(clerk -> Boolean.TRUE.equals(clerk.getIsActive()))
                    .toList();
    }

    private List<Advocate> search(RequestInfo requestInfo, AdvocateSearchCriteria criteria) {
        List<AdvocateSearchCriteria> criteriaList = new ArrayList<>();
        criteriaList.add(criteria);
        advocateService.searchAdvocate(requestInfo, criteriaList, null, 10, 0);
        return criteria.getResponseList() == null
                ? List.of()
                : criteria.getResponseList().stream().filter(Advocate::getIsActive).toList();
    }
}
