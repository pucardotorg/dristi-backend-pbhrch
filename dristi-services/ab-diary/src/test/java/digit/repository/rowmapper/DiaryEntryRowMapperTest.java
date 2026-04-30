package digit.repository.rowmapper;

import digit.web.models.AuditDetails;
import digit.web.models.CaseDiaryEntry;

import org.egov.tracer.model.CustomException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DiaryEntryRowMapperTest {

    @Mock
    private ResultSet resultSet;

    @InjectMocks
    private DiaryEntryRowMapper rowMapper;

    private final String TEST_UUID = "123e4567-e89b-12d3-a456-426614174000";
    private final String TENANT_ID = "default";
    private final long ENTRY_DATE_MILLIS = 1234567890L;
    private final OffsetDateTime ENTRY_DATE = OffsetDateTime.ofInstant(Instant.ofEpochMilli(ENTRY_DATE_MILLIS), ZoneOffset.UTC);
    private final String CASE_NUMBER = "CASE-123";
    private final String COURT_ID = "COURT-123";
    private final String BUSINESS_OF_DAY = "Regular hearing";
    private final String REFERENCE_ID = "REF-123";
    private final String REFERENCE_TYPE = "HEARING";
    private final long HEARING_DATE_MILLIS = 1234567899L;
    private final OffsetDateTime HEARING_DATE = OffsetDateTime.ofInstant(Instant.ofEpochMilli(HEARING_DATE_MILLIS), ZoneOffset.UTC);
    private final OffsetDateTime CREATED_TIME = OffsetDateTime.now().minusSeconds(200);
    private final String CREATED_BY = "CREATOR-123";
    private final OffsetDateTime MODIFIED_TIME = OffsetDateTime.now();
    private final String MODIFIED_BY = "MODIFIER-123";

    @BeforeEach
    void setUp() throws SQLException {
        // Default behavior for resultSet.next()
        when(resultSet.next()).thenReturn(true, false);
    }

    @Test
    void extractData_Success() throws SQLException, DataAccessException {
        // Arrange
        setupValidResultSet();

        // Act
        List<CaseDiaryEntry> result = rowMapper.extractData(resultSet);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());

        CaseDiaryEntry entry = result.get(0);
        assertEquals(UUID.fromString(TEST_UUID), entry.getId());
        assertEquals(TENANT_ID, entry.getTenantId());
        assertEquals(ENTRY_DATE, entry.getEntryDate());
        assertEquals(CASE_NUMBER, entry.getCaseNumber());
        assertEquals(COURT_ID, entry.getCourtId());
        assertEquals(BUSINESS_OF_DAY, entry.getBusinessOfDay());
        assertEquals(REFERENCE_ID, entry.getReferenceId());
        assertEquals(REFERENCE_TYPE, entry.getReferenceType());
        assertEquals(HEARING_DATE, entry.getHearingDate());

        AuditDetails auditDetails = entry.getAuditDetails();
        assertNotNull(auditDetails);
        assertNotNull(auditDetails.getCreatedTime());
        assertEquals(CREATED_BY, auditDetails.getCreatedBy());
        assertNotNull(auditDetails.getLastModifiedTime());
        assertEquals(MODIFIED_BY, auditDetails.getLastModifiedBy());

        // Verify
        verify(resultSet, times(2)).next();
        verifyResultSetReads();
    }

    @Test
    void DataWithEmptyHearingDate_Success() throws SQLException {
        setupValidResultSet();
        when(resultSet.getTimestamp("hearingDate")).thenReturn(null);

        // Act
        List<CaseDiaryEntry> result = rowMapper.extractData(resultSet);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());

        CaseDiaryEntry entry = result.get(0);
        assertEquals(UUID.fromString(TEST_UUID), entry.getId());
        assertEquals(TENANT_ID, entry.getTenantId());
        assertEquals(ENTRY_DATE, entry.getEntryDate());
        assertEquals(CASE_NUMBER, entry.getCaseNumber());
        assertEquals(COURT_ID, entry.getCourtId());
        assertEquals(BUSINESS_OF_DAY, entry.getBusinessOfDay());
        assertEquals(REFERENCE_ID, entry.getReferenceId());
        assertEquals(REFERENCE_TYPE, entry.getReferenceType());
        assertNull(entry.getHearingDate());

        AuditDetails auditDetails = entry.getAuditDetails();
        assertNotNull(auditDetails);
        assertNotNull(auditDetails.getCreatedTime());
        assertEquals(CREATED_BY, auditDetails.getCreatedBy());
        assertNotNull(auditDetails.getLastModifiedTime());
        assertEquals(MODIFIED_BY, auditDetails.getLastModifiedBy());

        // Verify
        verify(resultSet, times(2)).next();
    }

    @Test
    void extractData_EmptyResultSet_ReturnsEmptyList() throws SQLException, DataAccessException {
        // Arrange
        when(resultSet.next()).thenReturn(false);

        // Act
        List<CaseDiaryEntry> result = rowMapper.extractData(resultSet);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());

        // Verify
        verify(resultSet).next();
    }

    @Test
    void extractData_SQLExceptionThrown_ThrowsCustomException() throws SQLException {
        // Arrange
        when(resultSet.next()).thenThrow(new SQLException("Database error"));

        // Act & Assert
        CustomException exception = assertThrows(CustomException.class,
                () -> rowMapper.extractData(resultSet));
        assertTrue(exception.getMessage()
                .contains("Error occurred while processing document ResultSet"));

        // Verify
        verify(resultSet).next();
    }

    @Test
    void extractData_DataAccessExceptionThrown_ThrowsCustomException() throws SQLException {
        // Arrange
        when(resultSet.next()).thenThrow(new DataAccessException("Data access error") {
        });

        // Act & Assert
        CustomException exception = assertThrows(CustomException.class,
                () -> rowMapper.extractData(resultSet));
        assertTrue(exception.getMessage()
                .contains("Error occurred while processing document ResultSet"));

        // Verify
        verify(resultSet).next();
    }

    private void setupValidResultSet() throws SQLException {
        when(resultSet.getString("id")).thenReturn(TEST_UUID);
        when(resultSet.getString("tenantId")).thenReturn(TENANT_ID);
        when(resultSet.getTimestamp("entryDate")).thenReturn(new Timestamp(ENTRY_DATE_MILLIS));
        when(resultSet.getString("caseNumber")).thenReturn(CASE_NUMBER);
        when(resultSet.getString("courtId")).thenReturn(COURT_ID);
        when(resultSet.getString("businessOfDay")).thenReturn(BUSINESS_OF_DAY);
        when(resultSet.getString("referenceId")).thenReturn(REFERENCE_ID);
        when(resultSet.getString("referenceType")).thenReturn(REFERENCE_TYPE);
        when(resultSet.getTimestamp("hearingDate")).thenReturn(new Timestamp(HEARING_DATE_MILLIS));
        when(resultSet.getTimestamp("createdTime")).thenReturn(new Timestamp(CREATED_TIME.toInstant().toEpochMilli()));
        when(resultSet.getString("createdBy")).thenReturn(CREATED_BY);
        when(resultSet.getTimestamp("lastModifiedTime")).thenReturn(new Timestamp(MODIFIED_TIME.toInstant().toEpochMilli()));
        when(resultSet.getString("lastModifiedBy")).thenReturn(MODIFIED_BY);
        when(resultSet.getString("caseId")).thenReturn("caseId");
    }

    private void verifyResultSetReads() throws SQLException {
        verify(resultSet).getString("id");
        verify(resultSet).getString("tenantId");
        verify(resultSet, times(2)).getTimestamp("entryDate");
        verify(resultSet).getString("caseNumber");
        verify(resultSet).getString("courtId");
        verify(resultSet).getString("businessOfDay");
        verify(resultSet).getString("referenceId");
        verify(resultSet).getString("referenceType");
        verify(resultSet, times(2)).getTimestamp("hearingDate");
        verify(resultSet, atLeastOnce()).getTimestamp("createdTime");
        verify(resultSet).getString("createdBy");
        verify(resultSet, atLeastOnce()).getTimestamp("lastModifiedTime");
        verify(resultSet).getString("lastModifiedBy");
    }
}
