package org.pucar.dristi.caselifecycle.order.internal.validators;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pucar.dristi.caselifecycle.cases.CaseApi;
import org.pucar.dristi.caselifecycle.cases.internal.web.models.CaseExists;
import org.pucar.dristi.caselifecycle.cases.internal.web.models.CaseExistsResponse;
import org.pucar.dristi.caselifecycle.order.internal.config.Configuration;
import org.pucar.dristi.caselifecycle.order.internal.config.MdmsDataConfig;
import org.pucar.dristi.caselifecycle.order.internal.repository.OrderRepository;
import org.pucar.dristi.common.util.FileStoreUtil;
import org.pucar.dristi.common.util.MdmsUtil;
import org.pucar.dristi.caselifecycle.order.internal.web.models.*;
import org.pucar.dristi.common.contract.order.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.pucar.dristi.caselifecycle.order.internal.config.ServiceConstants.*;

import org.pucar.dristi.common.models.Document;
@ExtendWith(MockitoExtension.class)
class OrderRegistrationValidatorTest {

    @Mock
    private OrderRepository repository;

    @Mock
    private CaseApi caseApi;

    @Mock
    private FileStoreUtil fileStoreUtil;

    @Mock
    private MdmsUtil mdmsUtil;

    @Mock
    private Configuration configuration;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private MdmsDataConfig mdmsDataConfig;

    @InjectMocks
    private OrderRegistrationValidator orderRegistrationValidator;

    @BeforeEach
    void setUp() {
        orderRegistrationValidator = new OrderRegistrationValidator(repository, caseApi, fileStoreUtil,configuration,mdmsUtil,objectMapper,mdmsDataConfig);
    }

    @Test
    void testValidateOrderRegistration_success() {
        // Prepare test data
        Order order = new Order();
        order.setStatuteSection(new StatuteSection());
        order.setOrderCategory("Judicial");
        order.setCnrNumber("CNR12345");
        order.setFilingNumber("FIL12345");
        order.setOrderType("Interim"); // Added orderType field

        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setRequestInfo(new RequestInfo());
        orderRequest.setOrder(order);

        // Mock behavior — caseApi.exists returns a response whose first criterion has exists=true
        CaseExists matched = new CaseExists();
        matched.setExists(true);
        CaseExistsResponse mockResp = CaseExistsResponse.builder()
                .criteria(Collections.singletonList(matched)).build();
        when(caseApi.exists(any())).thenReturn(mockResp);

        // Execute method
        assertDoesNotThrow(() -> orderRegistrationValidator.validateOrderRegistration(orderRequest));

        // Verify
        verify(caseApi).exists(any());
    }

    @Test
    void testValidateOrderRegistration_missingStatuteSection() {
        // Prepare test data
        Order order = new Order();
        order.setOrderCategory("Judicial");
        order.setOrderType("Interim"); // Added orderType field

        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setRequestInfo(new RequestInfo());
        orderRequest.setOrder(order);

        // Execute and verify exception
        CustomException exception = assertThrows(CustomException.class, () ->
                orderRegistrationValidator.validateOrderRegistration(orderRequest));
        assertEquals(CREATE_ORDER_ERR, exception.getCode());
        assertEquals("statute and section is mandatory for creating order", exception.getMessage());
    }

    @Test
    void testValidateOrderRegistration_invalidCase() {
        // Prepare test data
        Order order = new Order();
        order.setStatuteSection(new StatuteSection());
        order.setOrderCategory("Judicial");
        order.setCnrNumber("CNR12345");
        order.setFilingNumber("FIL12345");
        order.setOrderType("Interim"); // Added orderType field

        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setRequestInfo(new RequestInfo());
        orderRequest.setOrder(order);

        // Mock behavior — caseApi.exists returns a response whose first criterion has exists=false
        CaseExists notMatched = new CaseExists();
        notMatched.setExists(false);
        CaseExistsResponse mockResp = CaseExistsResponse.builder()
                .criteria(Collections.singletonList(notMatched)).build();
        when(caseApi.exists(any())).thenReturn(mockResp);

        // Execute and verify exception
        CustomException exception = assertThrows(CustomException.class, () ->
                orderRegistrationValidator.validateOrderRegistration(orderRequest));
        assertEquals("INVALID_CASE_DETAILS", exception.getCode());
        assertEquals("Invalid Case", exception.getMessage());
    }

    @Test
    void testValidateOrderRegistration_administrativeOrderSuccess() {
        // Prepare test data - new test case for Administrative order
        Order order = new Order();
        order.setStatuteSection(new StatuteSection());
        order.setOrderCategory("Administrative");
        order.setOrderType("Interim");
        // No CNR or filing number needed for Administrative orders

        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setRequestInfo(new RequestInfo());
        orderRequest.setOrder(order);

        // Execute method - should not call fetchCaseDetails for Administrative orders
        assertDoesNotThrow(() -> orderRegistrationValidator.validateOrderRegistration(orderRequest));

        // Verify that caseApi.exists was not called for Administrative orders
        verify(caseApi, never()).exists(any());
    }

    @Test
    void testValidateApplicationExistence_true() {
        // Prepare test data
        Order order = new Order();
        order.setId(UUID.randomUUID());
        order.setCnrNumber("CNR12345");
        order.setFilingNumber("FIL12345");

        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setOrder(order);

        OrderExists orderExists = new OrderExists();
        orderExists.setExists(true);
        List<OrderExists> orderExistsList = new ArrayList<>();
        orderExistsList.add(orderExists);

        // Mock behavior
        when(repository.checkOrderExists(anyList())).thenReturn(orderExistsList);

        // Execute method
        boolean result = orderRegistrationValidator.validateApplicationExistence(orderRequest);

        // Verify
        assertTrue(result);
        verify(repository).checkOrderExists(anyList());
    }

    @Test
    void testValidateApplicationExistence_false() {
        // Prepare test data
        Order order = new Order();
        order.setId(UUID.randomUUID());
        order.setCnrNumber("CNR12345");
        order.setFilingNumber("FIL12345");

        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setOrder(order);

        List<OrderExists> orderExistsList = new ArrayList<>();

        // Mock behavior
        when(repository.checkOrderExists(anyList())).thenReturn(orderExistsList);

        // Execute method
        boolean result = orderRegistrationValidator.validateApplicationExistence(orderRequest);

        // Verify
        assertFalse(result);
        verify(repository).checkOrderExists(anyList());
    }

    @Test
    void testValidateDocuments_success() {
        // Prepare test data
        Order order = new Order();
        Document document = new Document();
        document.setFileStore("fileStoreId");
        List<Document> documents = new ArrayList<>();
        documents.add(document);
        order.setDocuments(documents);
        order.setTenantId("pg");
        order.setFilingNumber("123");
        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setOrder(order);

        // Mock behavior
        when(fileStoreUtil.doesFileExist(anyString(), eq("fileStoreId"))).thenReturn(true);

        // Execute method
        assertDoesNotThrow(() -> orderRegistrationValidator.validateApplicationExistence(orderRequest));

        // Verify
        verify(fileStoreUtil).doesFileExist(anyString(), eq("fileStoreId"));
    }

    @Test
    void testValidateDocuments_invalidFileStore() {
        // Prepare test data
        Order order = new Order();
        Document document = new Document();
        document.setFileStore("fileStoreId");
        List<Document> documents = new ArrayList<>();
        documents.add(document);
        order.setDocuments(documents);
        order.setTenantId("pg");
        order.setFilingNumber("123");
        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setOrder(order);

        // Mock behavior
        when(fileStoreUtil.doesFileExist(anyString(), eq("fileStoreId"))).thenReturn(false);

        // Execute and verify exception
        CustomException exception = assertThrows(CustomException.class, () ->
                orderRegistrationValidator.validateApplicationExistence(orderRequest));
        assertEquals(INVALID_FILESTORE_ID, exception.getCode());
        assertEquals(INVALID_DOCUMENT_DETAILS, exception.getMessage());
    }

    @Test
    void testValidateDocuments_missingFileStore() {
        // Prepare test data
        Order order = new Order();
        Document document = new Document();
        List<Document> documents = new ArrayList<>();
        documents.add(document);
        order.setDocuments(documents);
        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setOrder(order);

        // Execute and verify exception
        CustomException exception = assertThrows(CustomException.class, () ->
                orderRegistrationValidator.validateApplicationExistence(orderRequest));
        assertEquals(INVALID_FILESTORE_ID, exception.getCode());
        assertEquals(INVALID_DOCUMENT_DETAILS, exception.getMessage());
    }
}
