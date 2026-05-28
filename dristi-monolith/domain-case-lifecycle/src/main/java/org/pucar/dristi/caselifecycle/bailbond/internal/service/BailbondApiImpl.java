package org.pucar.dristi.caselifecycle.bailbond.internal.service;

import org.egov.common.contract.response.ResponseInfo;
import org.pucar.dristi.caselifecycle.bailbond.BailbondApi;
import org.pucar.dristi.common.contract.bailbond.Bail;
import org.pucar.dristi.common.contract.bailbond.BailSearchRequest;
import org.pucar.dristi.common.contract.bailbond.BailSearchResponse;
import org.pucar.dristi.common.util.ResponseInfoFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BailbondApiImpl implements BailbondApi {

    private final BailService bailService;
    private final ResponseInfoFactory responseInfoFactory;

    @Autowired
    public BailbondApiImpl(BailService bailService, ResponseInfoFactory responseInfoFactory) {
        this.bailService = bailService;
        this.responseInfoFactory = responseInfoFactory;
    }

    @Override
    public BailSearchResponse searchBail(BailSearchRequest request) {
        List<Bail> bails = bailService.searchBail(request);
        ResponseInfo responseInfo = responseInfoFactory.createResponseInfoFromRequestInfo(request.getRequestInfo(), true);
        return BailSearchResponse.builder()
                .bails(bails)
                .responseInfo(responseInfo)
                .pagination(request.getPagination())
                .build();
    }
}
