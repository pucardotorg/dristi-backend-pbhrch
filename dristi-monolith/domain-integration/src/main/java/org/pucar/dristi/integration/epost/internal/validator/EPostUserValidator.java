package org.pucar.dristi.integration.epost.internal.validator;

import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.pucar.dristi.integration.epost.internal.config.MdmsDataConfig;
import org.pucar.dristi.common.contract.epost.EPostTrackerSearchRequest;
import org.pucar.dristi.common.util.MdmsUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
public class EPostUserValidator {

    private final MdmsDataConfig mdmsDataConfig;

    @Autowired
    public EPostUserValidator(MdmsDataConfig mdmsDataConfig) {
        this.mdmsDataConfig = mdmsDataConfig;
    }

    public String getPostalHubName(EPostTrackerSearchRequest searchRequest) {
        RequestInfo requestInfo = searchRequest.getRequestInfo();
        String userName = requestInfo.getUserInfo().getUserName();
        Map<String,String> postalHubAndUserNameMap = mdmsDataConfig.getPostalHubUserNameMap();
        return postalHubAndUserNameMap.get(userName);
    }

}
