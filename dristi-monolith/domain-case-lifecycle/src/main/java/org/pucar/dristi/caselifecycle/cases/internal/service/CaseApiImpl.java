package org.pucar.dristi.caselifecycle.cases.internal.service;

import java.util.Collections;
import java.util.List;

import org.egov.common.contract.response.ResponseInfo;
import org.pucar.dristi.caselifecycle.cases.CaseApi;
import org.pucar.dristi.caselifecycle.cases.internal.web.models.CaseExists;
import org.pucar.dristi.caselifecycle.cases.internal.web.models.CaseExistsRequest;
import org.pucar.dristi.caselifecycle.cases.internal.web.models.CaseExistsResponse;
import org.pucar.dristi.caselifecycle.cases.internal.web.models.CaseListResponse;
import org.pucar.dristi.caselifecycle.cases.internal.web.models.CaseSearchRequest;
import org.pucar.dristi.common.util.ResponseInfoFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CaseApiImpl implements CaseApi {

    private final CaseService caseService;
    private final ResponseInfoFactory responseInfoFactory;

    @Autowired
    public CaseApiImpl(CaseService caseService, ResponseInfoFactory responseInfoFactory) {
        this.caseService = caseService;
        this.responseInfoFactory = responseInfoFactory;
    }

    @Override
    public CaseExistsResponse exists(CaseExistsRequest request) {
        List<CaseExists> caseExists = caseService.existCases(request);
        ResponseInfo responseInfo = responseInfoFactory.createResponseInfoFromRequestInfo(
                request.getRequestInfo(), true);
        return CaseExistsResponse.builder()
                .criteria(caseExists != null ? caseExists : Collections.emptyList())
                .responseInfo(responseInfo)
                .build();
    }

    @Override
    public CaseListResponse search(CaseSearchRequest request) {
        caseService.searchCases(request);
        ResponseInfo responseInfo = responseInfoFactory.createResponseInfoFromRequestInfo(
                request.getRequestInfo(), true);
        return CaseListResponse.builder()
                .criteria(request.getCriteria())
                .responseInfo(responseInfo)
                .build();
    }
}
