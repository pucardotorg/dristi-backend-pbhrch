package digit.repository.rowmapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import digit.util.DateUtil;
import digit.web.models.AuditDetails;
import digit.web.models.OptOut;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;

@ExtendWith(MockitoExtension.class)
public class RescheduleRequestOptOutRowMapperTest {

    @InjectMocks
    private RescheduleRequestOptOutRowMapper mapper;

    @Mock
    private ResultSet resultSet;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private DateUtil dateUtil;

    @BeforeEach
    public void setUp() {
        Mockito.lenient().when(dateUtil.timestampToOffsetDateTime(org.mockito.ArgumentMatchers.any()))
                .thenAnswer(invocation -> {
                    Timestamp ts = invocation.getArgument(0);
                    return ts == null ? null : ts.toInstant().atOffset(ZoneOffset.UTC);
                });
    }

    @Test
    public void testMapRow() throws SQLException, JsonProcessingException {

        Timestamp createdTimestamp = Timestamp.from(Instant.now());
        Timestamp lastModifiedTimestamp = Timestamp.from(Instant.now());

        when(resultSet.getString("judge_id")).thenReturn("J001");
        when(resultSet.getString("id")).thenReturn("id");
        when(resultSet.getString("case_id")).thenReturn("CASE001");
        when(resultSet.getString("reschedule_request_id")).thenReturn("RR001");
        when(resultSet.getString("individual_id")).thenReturn("IND001");
        when(resultSet.getInt("row_version")).thenReturn(1);
        when(resultSet.getTimestamp("created_time")).thenReturn(createdTimestamp);
        when(resultSet.getTimestamp("last_modified_time")).thenReturn(lastModifiedTimestamp);

        OptOut optOut = mapper.mapRow(resultSet, 1);

        assertEquals("J001", optOut.getJudgeId());
        assertEquals("CASE001", optOut.getCaseId());
        assertEquals("RR001", optOut.getRescheduleRequestId());
        assertEquals("IND001", optOut.getIndividualId());

        // Verify AuditDetails
        AuditDetails auditDetails = optOut.getAuditDetails();
        assertNotNull(auditDetails.getCreatedTime());
        assertNotNull(auditDetails.getLastModifiedTime());

        assertEquals(1, optOut.getRowVersion());
    }

    @Test
    public void testMapRowWithNullValues() throws SQLException, JsonProcessingException {
        lenient().when(resultSet.getString("judge_id")).thenReturn(null);
        lenient().when(resultSet.getString("id")).thenReturn("id");
        lenient().when(resultSet.getString("case_id")).thenReturn("CASE002");
        lenient().when(resultSet.getString("reschedule_request_id")).thenReturn("RR002");
        lenient().when(resultSet.getString("individual_id")).thenReturn("IND002");
        lenient().when(resultSet.getString("opt_out_dates")).thenReturn(null);
        lenient().when(resultSet.getInt("row_version")).thenReturn(2);
        lenient().when(resultSet.getTimestamp("created_time")).thenReturn(Timestamp.from(Instant.now()));
        lenient().when(resultSet.getTimestamp("last_modified_time")).thenReturn(Timestamp.from(Instant.now()));

        OptOut optOut = mapper.mapRow(resultSet, 1);

        assertEquals(null, optOut.getJudgeId()); // Expected to be null
        assertEquals("CASE002", optOut.getCaseId());
        assertEquals("RR002", optOut.getRescheduleRequestId());
        assertEquals("IND002", optOut.getIndividualId());
        assertEquals(null, optOut.getOptoutDates());
        assertEquals(2, optOut.getRowVersion());
    }

    @Test
    public void testMapRowWithJsonProcessingException() throws SQLException, JsonProcessingException {
        // Mock data for ResultSet
        lenient().when(resultSet.getString("judge_id")).thenReturn("J003");
        lenient().when(resultSet.getString("case_id")).thenReturn("CASE003");
        lenient().when(resultSet.getString("reschedule_request_id")).thenReturn("RR003");
        lenient().when(resultSet.getString("individual_id")).thenReturn("IND003");
        lenient().when(resultSet.getString("opt_out_dates")).thenReturn("[\"2024-07-07\",\"2024-07-08\"]");
        lenient().when(resultSet.getInt("row_version")).thenReturn(3);
        lenient().when(resultSet.getTimestamp("created_time")).thenReturn(Timestamp.from(Instant.now()));
        lenient().when(resultSet.getTimestamp("last_modified_time")).thenReturn(Timestamp.from(Instant.now()));
        lenient().when(resultSet.getString("tenant_id")).thenReturn("T003");
        Mockito.when(objectMapper.readValue(org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.<com.fasterxml.jackson.core.type.TypeReference<List<Long>>>any()))
                .thenThrow(new JsonProcessingException("test"){});

        RuntimeException exception = org.junit.jupiter.api.Assertions.assertThrows(
                RuntimeException.class,
                () -> mapper.mapRow(resultSet, 1)
        );
    }
}
