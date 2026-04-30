package digit.repository.rowmapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import digit.util.DateUtil;
import digit.web.models.AuditDetails;
import digit.web.models.JudgeCalendarRule;
import digit.web.models.enums.JudgeRuleType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CalendarRowMapperTest {

    @InjectMocks
    private CalendarRowMapper mapper;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ResultSet resultSet;

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
    public void testMapRow() throws SQLException {
        Timestamp createdTimestamp = Timestamp.from(Instant.now());
        Timestamp lastModifiedTimestamp = Timestamp.from(Instant.now());

        // Mock data for ResultSet
        when(resultSet.getString("id")).thenReturn("1");
        when(resultSet.getString("judge_id")).thenReturn("J001");
        when(resultSet.getString("rule_type")).thenReturn("LEAVE");
        when(resultSet.getTimestamp("date")).thenReturn(Timestamp.from(Instant.now()));
        when(resultSet.getString("notes")).thenReturn("Sample note");
        when(resultSet.getString("tenant_id")).thenReturn("T001");
        when(resultSet.getString("created_by")).thenReturn("admin");
        when(resultSet.getTimestamp("created_time")).thenReturn(createdTimestamp);
        when(resultSet.getString("last_modified_by")).thenReturn("admin");
        when(resultSet.getTimestamp("last_modified_time")).thenReturn(lastModifiedTimestamp);
        when(resultSet.getInt("row_version")).thenReturn(1);
        when(resultSet.getString("court_ids")).thenReturn("[\"C001\", \"C002\"]");

        // Call mapRow and validate
        JudgeCalendarRule calendarRule = mapper.mapRow(resultSet, 1);

        // Assert the mapped values
        assertEquals("1", calendarRule.getId());
        assertEquals("J001", calendarRule.getJudgeId());
        assertEquals(JudgeRuleType.LEAVE.toString(), calendarRule.getRuleType());
        assertEquals("Sample note", calendarRule.getNotes());
        assertEquals("T001", calendarRule.getTenantId());

        AuditDetails auditDetails = calendarRule.getAuditDetails();
        assertEquals("admin", auditDetails.getCreatedBy());
        assertEquals("admin", auditDetails.getLastModifiedBy());
        assertNotNull(auditDetails.getCreatedTime());
        assertNotNull(auditDetails.getLastModifiedTime());

        assertEquals(1, calendarRule.getRowVersion());
    }

}
