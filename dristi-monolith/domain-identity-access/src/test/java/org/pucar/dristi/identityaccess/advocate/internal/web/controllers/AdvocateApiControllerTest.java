package org.pucar.dristi.identityaccess.advocate.internal.web.controllers;

import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.User;
import org.egov.common.contract.response.ResponseInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pucar.dristi.identityaccess.advocate.internal.service.AdvocateService;
import org.pucar.dristi.common.util.ResponseInfoFactory;
import org.pucar.dristi.identityaccess.advocate.internal.web.models.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdvocateApiControllerTest {

    @InjectMocks
    private AdvocateApiController advocateApiController;

    @Mock
    private AdvocateService advocateService;

    @Mock
    private ResponseInfoFactory responseInfoFactory;

    @Test
    void testAdvocateV1CreatePost() {
        AdvocateRequest request = new AdvocateRequest();
        RequestInfo requestInfo = new RequestInfo();
        request.setRequestInfo(requestInfo);

        Advocate advocate = new Advocate();
        List<Advocate> advocateList = Arrays.asList(new Advocate());
        when(advocateService.createAdvocate(request)).thenReturn(advocate);
        when(responseInfoFactory.createResponseInfoFromRequestInfo(requestInfo, true)).thenReturn(new ResponseInfo());

        ResponseEntity<AdvocateResponse> responseEntity = advocateApiController.advocateV1CreatePost(request);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(advocateList, responseEntity.getBody().getAdvocates());
    }

    @Test
    void testAdvocateV1StatusSearchPost() {
        String status = "active";
        String tenantId = "tenantId";
        List<Advocate> advocateList = new ArrayList<>();
        AdvocateSimpleSearchRequest advocateSimpleSearchRequest = new AdvocateSimpleSearchRequest();
        RequestInfo requestInfo = new RequestInfo();
        User userInfo = new User();
        userInfo.setType("EMPLOYEE");
        userInfo.setUuid(UUID.randomUUID().toString());
        requestInfo.setUserInfo(userInfo);
        advocateSimpleSearchRequest.setRequestInfo(requestInfo);

        when(advocateService.searchAdvocateByStatus(advocateSimpleSearchRequest, status, tenantId, 10, 0)).thenReturn(advocateList);
        when(responseInfoFactory.createResponseInfoFromRequestInfo(null, true)).thenReturn(new ResponseInfo());

        ResponseEntity<AdvocateResponse> responseEntity = advocateApiController.advocateV1StatusSearchPost(status, tenantId, 10, 0, advocateSimpleSearchRequest);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(advocateList, responseEntity.getBody().getAdvocates());
    }

    @Test
    void testAdvocateV1ApplicationnumberSearchPost() {
        String applicationNumber = "app123";
        String tenantId = "tenantId";
        List<Advocate> advocateList = Arrays.asList(new Advocate());
        RequestInfo requestInfo = new RequestInfo();
        User userInfo = new User();
        userInfo.setType("EMPLOYEE");
        userInfo.setUuid(UUID.randomUUID().toString());
        requestInfo.setUserInfo(userInfo);
        AdvocateSimpleSearchRequest advocateSimpleSearchRequest = new AdvocateSimpleSearchRequest();
        advocateSimpleSearchRequest.setRequestInfo(requestInfo);

        when(advocateService.searchAdvocateByApplicationNumber(requestInfo, applicationNumber, tenantId, 10, 0)).thenReturn(advocateList);
        when(responseInfoFactory.createResponseInfoFromRequestInfo(null, true)).thenReturn(new ResponseInfo());

        ResponseEntity<AdvocateResponse> responseEntity = advocateApiController.advocateV1ApplicationnumberSearchPost(applicationNumber, tenantId, 10, 0, advocateSimpleSearchRequest);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(advocateList, responseEntity.getBody().getAdvocates());
    }

    @Test
    void testAdvocateV1UpdatePost() {
        AdvocateRequest request = new AdvocateRequest();
        RequestInfo requestInfo = new RequestInfo();
        request.setRequestInfo(requestInfo);

        Advocate advocate = new Advocate();
        List<Advocate> advocateList = Arrays.asList(advocate);

        when(advocateService.updateAdvocate(request)).thenReturn(advocate);
        when(responseInfoFactory.createResponseInfoFromRequestInfo(requestInfo, true)).thenReturn(new ResponseInfo());

        ResponseEntity<AdvocateResponse> responseEntity = advocateApiController.advocateV1UpdatePost(request);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(advocateList, responseEntity.getBody().getAdvocates());
    }

    @Test
    void advocateV1SearchPost_Success() {
        AdvocateSearchRequest body = new AdvocateSearchRequest();
        RequestInfo requestInfo = new RequestInfo();
        body.setRequestInfo(requestInfo);
        List<AdvocateSearchCriteria> criteria = new ArrayList<>();
        body.setCriteria(criteria);
        body.setTenantId("tenant1");
        Integer limit = 10;
        Integer offset = 0;
        ResponseInfo responseInfo = new ResponseInfo();
        when(responseInfoFactory.createResponseInfoFromRequestInfo(requestInfo, true)).thenReturn(responseInfo);

        ResponseEntity<AdvocateListResponse> response = advocateApiController.advocateV1SearchPost(body, limit, offset);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(criteria, response.getBody().getAdvocates());
    }
}
