package org.pucar.dristi.caselifecycle.openapi.internal.util;

import static org.pucar.dristi.caselifecycle.openapi.internal.config.ServiceConstants.ERROR_WHILE_FETCHING_FROM_ADVOCATE;

import java.util.*;

import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.User;
import org.egov.tracer.model.CustomException;
import org.pucar.dristi.common.contract.openapi.Advocate;
import org.pucar.dristi.common.contract.openapi.AdvocateSearchCriteria;
import org.pucar.dristi.identityaccess.advocate.AdvocateApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component("openapiAdvocateUtil")
public class AdvocateUtil {

    private ObjectMapper mapper;

    private final AdvocateApi advocateApi;


    @Autowired
    public AdvocateUtil(ObjectMapper mapper, AdvocateApi advocateApi) {
        this.mapper = mapper;
        this.advocateApi = advocateApi;
    }

    public List<Advocate> fetchAdvocates(AdvocateSearchCriteria advocateSearchCriteria) {
        try {
            RequestInfo requestInfo = RequestInfo.builder().userInfo(User.builder().build()).build();
            List<org.pucar.dristi.common.contract.advocate.Advocate> advocates =
                    advocateApi.searchAdvocatesByBarRegistrationNumber(requestInfo,
                            advocateSearchCriteria.getBarRegistrationNumber());
            return advocates.stream()
                    .map(advocate -> mapper.convertValue(advocate, Advocate.class))
                    .toList();
        } catch (Exception e) {
            log.error(ERROR_WHILE_FETCHING_FROM_ADVOCATE, e);
            throw new CustomException(ERROR_WHILE_FETCHING_FROM_ADVOCATE, e.getMessage());
        }
    }

    public List<Advocate> fetchAdvocatesByBarRegistrationNumber(String barRegistrationNumber) {

        AdvocateSearchCriteria advocateSearchCriteria = new AdvocateSearchCriteria();
        advocateSearchCriteria.setBarRegistrationNumber(barRegistrationNumber);

        return fetchAdvocates(advocateSearchCriteria);

    }

}