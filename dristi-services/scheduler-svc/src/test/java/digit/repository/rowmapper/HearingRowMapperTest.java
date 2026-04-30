package digit.repository.rowmapper;

import digit.util.DateUtil;
import digit.web.models.AuditDetails;
import digit.web.models.ScheduleHearing;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class HearingRowMapperTest {

    @InjectMocks
    private HearingRowMapper mapper;

    @Mock
    private ResultSet resultSet;

    @Mock
    private DateUtil dateUtil;

    @org.mockito.Captor
    private org.mockito.ArgumentCaptor<Timestamp> timestampCaptor;

    @BeforeEach
    public void setUp() {
        org.mockito.Mockito.lenient().when(dateUtil.timestampToOffsetDateTime(org.mockito.ArgumentMatchers.any()))
                .thenAnswer(invocation -> {
                    Timestamp ts = invocation.getArgument(0);
                    return ts == null ? null : ts.toInstant().atOffset(ZoneOffset.UTC);
                });
    }

    @Test
    public void testMapRow() throws SQLException {
        Timestamp createdTimestamp = Timestamp.from(Instant.now());
        Timestamp lastModifiedTimestamp = Timestamp.from(Instant.now());

        // Mock data for ResultSet
        when(resultSet.getString("description")).thenReturn("Sample hearing description");
        when(resultSet.getString("hearing_booking_id")).thenReturn("HB001");
        when(resultSet.getString("tenant_id")).thenReturn("T001");
        when(resultSet.getString("court_id")).thenReturn("C001");
        when(resultSet.getString("judge_id")).thenReturn("J001");
        when(resultSet.getString("case_id")).thenReturn("CASE001");
        when(resultSet.getString("hearing_type")).thenReturn("ADMISSION");
        when(resultSet.getTimestamp("hearing_date")).thenReturn(Timestamp.from(Instant.now()));
        when(resultSet.getString("title")).thenReturn("Hearing Title");
        when(resultSet.getString("status")).thenReturn("SCHEDULED");
        when(resultSet.getTimestamp("start_time")).thenReturn(Timestamp.from(Instant.now()));
        when(resultSet.getTimestamp("end_time")).thenReturn(Timestamp.from(Instant.now()));
        when(resultSet.getString("reschedule_request_id")).thenReturn("R001");
        when(resultSet.getString("created_by")).thenReturn("admin");
        when(resultSet.getTimestamp("created_time")).thenReturn(createdTimestamp);
        when(resultSet.getString("last_modified_by")).thenReturn("admin");
        when(resultSet.getTimestamp("last_modified_time")).thenReturn(lastModifiedTimestamp);
        when(resultSet.getInt("row_version")).thenReturn(1);
        when(resultSet.getString("case_stage")).thenReturn("case_stage");

        // Call mapRow and validate
        ScheduleHearing hearing = mapper.mapRow(resultSet, 1);

        // Assert the mapped values

        assertEquals("Sample hearing description", hearing.getDescription());
        assertEquals("HB001", hearing.getHearingBookingId());
        assertEquals("T001", hearing.getTenantId());
        assertEquals("C001", hearing.getCourtId());
        assertEquals("J001", hearing.getJudgeId());
        assertEquals("CASE001", hearing.getCaseId());
        assertEquals("ADMISSION", hearing.getHearingType());
        assertEquals("Hearing Title", hearing.getTitle());
        assertEquals("SCHEDULED", hearing.getStatus());

        // Verify AuditDetails
        AuditDetails auditDetails = hearing.getAuditDetails();
        assertEquals("admin", auditDetails.getCreatedBy());
        assertEquals("admin", auditDetails.getLastModifiedBy());
        assertNotNull(auditDetails.getCreatedTime());
        assertNotNull(auditDetails.getLastModifiedTime());

        assertEquals(1, hearing.getRowVersion());
    }

}
