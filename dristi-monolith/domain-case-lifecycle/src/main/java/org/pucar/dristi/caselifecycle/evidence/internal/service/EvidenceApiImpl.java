package org.pucar.dristi.caselifecycle.evidence.internal.service;

import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.response.ResponseInfo;
import org.pucar.dristi.caselifecycle.evidence.EvidenceApi;
import org.pucar.dristi.common.contract.evidence.Artifact;
import org.pucar.dristi.common.contract.evidence.EvidenceSearchCriteria;
import org.pucar.dristi.common.contract.evidence.EvidenceSearchResponse;
import org.pucar.dristi.common.contract.evidence.Pagination;
import org.pucar.dristi.common.util.ResponseInfoFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EvidenceApiImpl implements EvidenceApi {

    private final EvidenceService evidenceService;
    private final ResponseInfoFactory responseInfoFactory;

    @Autowired
    public EvidenceApiImpl(EvidenceService evidenceService, ResponseInfoFactory responseInfoFactory) {
        this.evidenceService = evidenceService;
        this.responseInfoFactory = responseInfoFactory;
    }

    @Override
    public EvidenceSearchResponse searchEvidence(RequestInfo requestInfo,
                                                 EvidenceSearchCriteria criteria,
                                                 Pagination pagination) {
        List<Artifact> artifacts = evidenceService.searchEvidence(requestInfo, criteria, pagination);
        ResponseInfo responseInfo = responseInfoFactory.createResponseInfoFromRequestInfo(requestInfo, true);
        return EvidenceSearchResponse.builder()
                .artifacts(artifacts)
                .responseInfo(responseInfo)
                .pagination(pagination)
                .build();
    }
}
