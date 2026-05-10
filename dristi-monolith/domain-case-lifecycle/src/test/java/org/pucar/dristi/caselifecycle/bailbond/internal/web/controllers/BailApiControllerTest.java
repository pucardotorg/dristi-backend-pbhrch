    package org.pucar.dristi.caselifecycle.bailbond.internal.web.controllers;

    import org.pucar.dristi.caselifecycle.bailbond.internal.service.BailService;
    import org.pucar.dristi.common.util.ResponseInfoFactory;
    import org.pucar.dristi.common.contract.bailbond.Bail;
    import org.pucar.dristi.common.contract.bailbond.BailRequest;
    import org.pucar.dristi.common.contract.bailbond.BailResponse;
    import org.pucar.dristi.common.contract.bailbond.BailSearchCriteria;
    import org.pucar.dristi.common.contract.bailbond.BailSearchRequest;
    import org.pucar.dristi.common.contract.bailbond.BailSearchResponse;
    import org.pucar.dristi.common.contract.bailbond.BailToSign;
    import org.pucar.dristi.common.contract.bailbond.BailsToSignRequest;
    import org.pucar.dristi.common.contract.bailbond.BailsToSignResponse;
    import org.pucar.dristi.common.contract.bailbond.UpdateSignedBailRequest;
    import org.pucar.dristi.common.contract.bailbond.UpdateSignedBailResponse;
    import org.egov.common.contract.request.RequestInfo;
    import org.egov.common.contract.response.ResponseInfo;
    import org.junit.jupiter.api.Test;
    import org.junit.jupiter.api.extension.ExtendWith;
    import org.mockito.InjectMocks;
    import org.mockito.Mock;
    import org.mockito.junit.jupiter.MockitoExtension;
    import org.springframework.http.HttpStatus;
    import org.springframework.http.ResponseEntity;

    import java.util.List;
    import java.util.Objects;

    import static org.junit.jupiter.api.Assertions.assertEquals;
    import static org.mockito.ArgumentMatchers.any;
    import static org.mockito.Mockito.when;

    /**
    * API tests for BailApiController
    */

    @ExtendWith(MockitoExtension.class)
    public class BailApiControllerTest {

        @InjectMocks
        private BailApiController bailApiController;
        @Mock
        private BailService bailService;
        @Mock
        private ResponseInfoFactory responseInfoFactory;


        @Test
        public void bailv1CreatePostSuccess() {

            BailRequest bailRequest = new BailRequest();
            Bail bail = new Bail();
            RequestInfo requestInfo = new RequestInfo();
            bailRequest.setBail(bail);
            bailRequest.setRequestInfo(requestInfo);

            //Mocking bailService.createBail method to return a Bail object
            when(bailService.createBail(bailRequest)).thenReturn(bail);

            // Mocking responseInfoFactory.createResponseInfoFromRequestInfo method to return a ResponseInfo object
            ResponseInfo responseInfo = new ResponseInfo();
            when(responseInfoFactory.createResponseInfoFromRequestInfo(requestInfo, true)).thenReturn(responseInfo);

            // Call the method under test
            ResponseEntity<BailResponse> bailResponseEntity = bailApiController.bailV1CreatePost(bailRequest);

            // Verify that response is correct
            assertEquals(responseInfo, Objects.requireNonNull(bailResponseEntity.getBody()).getResponseInfo());
            assertEquals(HttpStatus.OK, bailResponseEntity.getStatusCode());
            assertEquals(bail, Objects.requireNonNull(bailResponseEntity.getBody()).getBails().get(0));
        }


        @Test
        public void bailv1SearchPostSuccess() {
            BailSearchRequest bailSearchRequest = new BailSearchRequest();
            BailSearchCriteria criteria = BailSearchCriteria.builder()
                    .id("id")
                    .build();
            RequestInfo requestInfo = new RequestInfo();
            Bail bail = new Bail();
            bailSearchRequest.setCriteria(criteria);
            bailSearchRequest.setRequestInfo(requestInfo);

            //Mocking bailService.searchBail method to return a Bail object
            when(bailService.searchBail(bailSearchRequest)).thenReturn(List.of(bail));

            // Mocking responseInfoFactory.createResponseInfoFromRequestInfo method to return a ResponseInfo object
            ResponseInfo responseInfo = new ResponseInfo();
            when(responseInfoFactory.createResponseInfoFromRequestInfo(requestInfo, true)).thenReturn(responseInfo);

            // Call the method under test
            ResponseEntity<BailSearchResponse> bailResponseEntity = bailApiController.bailV1SearchPost(bailSearchRequest);

            // Verify that response is correct
            assertEquals(responseInfo, Objects.requireNonNull(bailResponseEntity.getBody()).getResponseInfo());
            assertEquals(HttpStatus.OK, bailResponseEntity.getStatusCode());
            assertEquals(bail, Objects.requireNonNull(bailResponseEntity.getBody()).getBails().get(0));

        }


        @Test
        public void v1UpdatePostSuccess() {
            BailRequest bailRequest = new BailRequest();
            Bail bail = new Bail();
            RequestInfo requestInfo = new RequestInfo();
            bailRequest.setBail(bail);
            bailRequest.setRequestInfo(requestInfo);

            //Mocking bailService.updateBail method to return a Bail object
            when(bailService.updateBail(bailRequest)).thenReturn(bail);

            // Mocking responseInfoFactory.createResponseInfoFromRequestInfo method to return a ResponseInfo object
            ResponseInfo responseInfo = new ResponseInfo();
            when(responseInfoFactory.createResponseInfoFromRequestInfo(requestInfo, true)).thenReturn(responseInfo);

            // Call the method under test
            ResponseEntity<BailResponse> bailResponseEntity = bailApiController.bailV1UpdatePost(bailRequest);

            // Verify that response is correct
            assertEquals(responseInfo, Objects.requireNonNull(bailResponseEntity.getBody()).getResponseInfo());
            assertEquals(HttpStatus.OK, bailResponseEntity.getStatusCode());
            assertEquals(bail, Objects.requireNonNull(bailResponseEntity.getBody()).getBails().get(0));
        }

        @Test
        public void getBailsToSignSuccess() {
            BailsToSignRequest request = new BailsToSignRequest();
            RequestInfo requestInfo = new RequestInfo();
            request.setRequestInfo(requestInfo);

            BailToSign bailToSign = new BailToSign();
            List<BailToSign> bailToSignList = List.of(bailToSign);

            ResponseInfo responseInfo = new ResponseInfo();

            when(bailService.createBailToSignRequest(any(BailsToSignRequest.class))).thenReturn(bailToSignList);
            when(responseInfoFactory.createResponseInfoFromRequestInfo(requestInfo, true)).thenReturn(responseInfo);

            ResponseEntity<BailsToSignResponse> responseEntity = bailApiController.getBailsToSign(request);

            assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
            assertEquals(responseInfo, Objects.requireNonNull(responseEntity.getBody()).getResponseInfo());
            assertEquals(bailToSignList, responseEntity.getBody().getBailList());
        }

        @Test
        public void updateSignedBailsSuccess() {
            UpdateSignedBailRequest request = new UpdateSignedBailRequest();
            RequestInfo requestInfo = new RequestInfo();
            request.setRequestInfo(requestInfo);

            Bail bail = new Bail();
            List<Bail> bails = List.of(bail);

            ResponseInfo responseInfo = new ResponseInfo();

            when(bailService.updateBailWithSignDoc(any(UpdateSignedBailRequest.class))).thenReturn(bails);
            when(responseInfoFactory.createResponseInfoFromRequestInfo(requestInfo, true)).thenReturn(responseInfo);

            ResponseEntity<UpdateSignedBailResponse> responseEntity = bailApiController.updateSignedBails(request);

            assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
            assertEquals(responseInfo, Objects.requireNonNull(responseEntity.getBody()).getResponseInfo());
            assertEquals(bails, responseEntity.getBody().getBails());
        }


    }
