package org.pucar.dristi.caselifecycle.transformer.internal.util;

import java.util.List;
import java.util.Set;

import org.egov.common.contract.request.RequestInfo;
import org.pucar.dristi.identityaccess.advocate.AdvocateApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component("transformerAdvocateUtil")
public class AdvocateUtil {

    private final AdvocateApi advocateApi;

    @Autowired
    public AdvocateUtil(AdvocateApi advocateApi) {
        this.advocateApi = advocateApi;
    }

    public Set<String> getAdvocate(RequestInfo requestInfo, List<String> advocateIds) {
        return advocateApi.getAdvocateIndividualIds(requestInfo, advocateIds);
    }
}
