package digit.enrichment;

import digit.config.Configuration;
import digit.util.IdgenUtil;
import digit.util.TaskManagementUtil;
import digit.web.models.TaskManagement;
import digit.web.models.TaskManagementRequest;
import digit.web.models.AuditDetails;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;

@ExtendWith(MockitoExtension.class)
class TaskManagementEnrichmentTest {

    @Mock
    private TaskManagementUtil taskManagementUtil;

    @Mock
    private IdgenUtil idgenUtil;

    @Mock
    private Configuration configuration;

    @Mock
    private digit.util.DateUtil dateUtil;

    @InjectMocks
    private TaskManagementEnrichment enrichment;

    private TaskManagementRequest request;
    private RequestInfo requestInfo;
    private TaskManagement taskManagement;
    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder().uuid("user-uuid-123").tenantId("pb").build();
        requestInfo = RequestInfo.builder().userInfo(user).build();
        taskManagement = TaskManagement.builder()
                .filingNumber("KL-2024-001")
                .tenantId("pb")
                .build();
        request = TaskManagementRequest.builder()
                .requestInfo(requestInfo)
                .taskManagement(taskManagement)
                .build();
        lenient().when(dateUtil.getCurrentOffsetDateTime()).thenReturn(OffsetDateTime.now());
    }

    @Test
    void enrichCreateRequest_SetsIdAndAuditDetails() {
        UUID testUuid = UUID.randomUUID();
        when(taskManagementUtil.generateUUID()).thenReturn(testUuid);
        when(configuration.getTaskManagementIdName()).thenReturn("task.management.id");
        when(configuration.getTaskManagementIdFormat()).thenReturn("TM-[SEQ]");
        when(idgenUtil.getIdList(any(), anyString(), anyString(), anyString(), anyInt(), anyBoolean()))
                .thenReturn(Collections.singletonList("001"));

        enrichment.enrichCreateRequest(request);

        assertEquals(testUuid.toString(), request.getTaskManagement().getId());
        assertNotNull(request.getTaskManagement().getAuditDetails());
        assertEquals("user-uuid-123", request.getTaskManagement().getAuditDetails().getCreatedBy());
        assertEquals("user-uuid-123", request.getTaskManagement().getAuditDetails().getLastModifiedBy());
        assertNotNull(request.getTaskManagement().getAuditDetails().getCreatedTime());
        assertNotNull(request.getTaskManagement().getAuditDetails().getLastModifiedTime());
    }

    @Test
    void enrichCreateRequest_SetsTaskManagementNumber() {
        UUID testUuid = UUID.randomUUID();
        when(taskManagementUtil.generateUUID()).thenReturn(testUuid);
        when(configuration.getTaskManagementIdName()).thenReturn("task.management.id");
        when(configuration.getTaskManagementIdFormat()).thenReturn("TM-[SEQ]");
        when(idgenUtil.getIdList(any(), anyString(), anyString(), anyString(), anyInt(), anyBoolean()))
                .thenReturn(Collections.singletonList("001"));

        enrichment.enrichCreateRequest(request);

        assertEquals("KL-2024-001-001", request.getTaskManagement().getTaskManagementNumber());
    }

    @Test
    void enrichUpdateRequest_SetsAuditDetailsForUpdate() {
        enrichment.enrichUpdateRequest(request);

        assertNotNull(request.getTaskManagement().getAuditDetails());
        assertEquals("user-uuid-123", request.getTaskManagement().getAuditDetails().getLastModifiedBy());
        assertNotNull(request.getTaskManagement().getAuditDetails().getLastModifiedTime());
    }

    @Test
    void enrichUpdateRequest_DoesNotSetCreatedBy() {
        enrichment.enrichUpdateRequest(request);

        AuditDetails auditDetails = request.getTaskManagement().getAuditDetails();
        assertNull(auditDetails.getCreatedBy());
        assertNull(auditDetails.getCreatedTime());
    }

    @Test
    void enrichCreateRequest_IdgenUtilCalledWithCorrectParams() {
        UUID testUuid = UUID.randomUUID();
        when(taskManagementUtil.generateUUID()).thenReturn(testUuid);
        when(configuration.getTaskManagementIdName()).thenReturn("task.management.id");
        when(configuration.getTaskManagementIdFormat()).thenReturn("TM-[SEQ]");
        when(idgenUtil.getIdList(any(), anyString(), anyString(), anyString(), anyInt(), anyBoolean()))
                .thenReturn(Collections.singletonList("001"));

        enrichment.enrichCreateRequest(request);

        verify(idgenUtil).getIdList(
                eq(requestInfo),
                eq("KL2024001"), // Filing number with dashes removed
                eq("task.management.id"),
                eq("TM-[SEQ]"),
                eq(1),
                eq(false)
        );
    }
}
