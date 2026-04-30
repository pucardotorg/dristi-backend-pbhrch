package digit.enrichment;

import digit.util.DigitalizedDocumentUtil;
import digit.web.models.AuditDetails;
import digit.web.models.DigitalizedDocument;
import digit.web.models.DigitalizedDocumentRequest;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;

class MediationEnrichmentTest {

    @Mock
    private DigitalizedDocumentUtil digitalizedDocumentUtil;

    @Mock
    private DigitalizedDocumentEnrichment digitalizedDocumentEnrichment;

    @InjectMocks
    private MediationEnrichment enrichment;

    private DigitalizedDocumentRequest request;
    private String userId;
    private OffsetDateTime currentTime;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        userId = UUID.randomUUID().toString();
        currentTime = OffsetDateTime.now(ZoneOffset.UTC);
        
        when(digitalizedDocumentUtil.getCurrentTimeOffset()).thenReturn(currentTime);
        when(digitalizedDocumentUtil.generateUUID()).thenReturn(UUID.randomUUID());
        
        request = DigitalizedDocumentRequest.builder()
                .requestInfo(RequestInfo.builder()
                        .userInfo(User.builder().uuid(userId).build())
                        .build())
                .digitalizedDocument(DigitalizedDocument.builder().build())
                .build();
    }

    @Test
    void testEnrichCreateMediationDocument() {
        // Execute
        enrichment.enrichCreateMediationDocument(request);
        
        // Verify
        DigitalizedDocument document = request.getDigitalizedDocument();
        assertNotNull(document.getId());
        
        // Verify audit details
        assertNotNull(document.getAuditDetails());
        assertEquals(userId, document.getAuditDetails().getCreatedBy());
        assertEquals(userId, document.getAuditDetails().getLastModifiedBy());
        assertEquals(currentTime, document.getAuditDetails().getCreatedTime());
        assertEquals(currentTime, document.getAuditDetails().getLastModifiedTime());
        
        // Verify digitalized document enrichment was called
        verify(digitalizedDocumentEnrichment, times(1))
                .enrichDigitalizedDocument(any(DigitalizedDocumentRequest.class));
    }

    @Test
    void testEnrichUpdateMediationDocument() {
        // Setup existing audit details
        OffsetDateTime createdTime = currentTime.minusSeconds(10);
        AuditDetails existingAudit = AuditDetails.builder()
                .createdBy("creator")
                .createdTime(createdTime)
                .lastModifiedBy("old-modifier")
                .lastModifiedTime(createdTime.plusSeconds(5))
                .build();
        request.getDigitalizedDocument().setAuditDetails(existingAudit);
        
        // Execute
        enrichment.enrichUpdateMediationDocument(request);
        
        // Verify
        AuditDetails updatedAudit = request.getDigitalizedDocument().getAuditDetails();
        assertNotNull(updatedAudit);
        
        // Verify created fields remain unchanged
        assertEquals("creator", updatedAudit.getCreatedBy());
        assertEquals(createdTime, updatedAudit.getCreatedTime());
        
        // Verify last modified fields are updated
        assertEquals(userId, updatedAudit.getLastModifiedBy());
        assertEquals(currentTime, updatedAudit.getLastModifiedTime());
    }
}
