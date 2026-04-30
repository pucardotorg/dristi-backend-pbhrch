package digit.repository.rowmapper;

import digit.util.DateUtil;
import digit.web.models.AuditDetails;
import digit.web.models.ScheduleHearing;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;

@Component
@Slf4j
public class HearingRowMapper implements RowMapper<ScheduleHearing> {

    private final DateUtil dateUtil;

    public HearingRowMapper(DateUtil dateUtil) {
        this.dateUtil = dateUtil;
    }

    @Override
    public ScheduleHearing mapRow(ResultSet resultSet, int rowNum) throws SQLException {

        ScheduleHearing hearing = ScheduleHearing.builder()
                .description(resultSet.getString("description"))
                .hearingBookingId(resultSet.getString("hearing_booking_id"))
                .tenantId(resultSet.getString("tenant_id"))
                .courtId(resultSet.getString("court_id"))
                .judgeId(resultSet.getString("judge_id"))
                .hearingType(resultSet.getString("hearing_type"))
                .caseId(resultSet.getString("case_id"))
                .title(resultSet.getString("title"))
                .status(resultSet.getString("status"))
                .hearingDate(resultSet.getTimestamp("hearing_date") != null ? resultSet.getTimestamp("hearing_date").toInstant().atOffset(java.time.ZoneOffset.UTC) : null)
                .startTime(resultSet.getTimestamp("start_time") != null ? resultSet.getTimestamp("start_time").toInstant().atOffset(java.time.ZoneOffset.UTC) : null)
                .endTime(resultSet.getTimestamp("end_time") != null ? resultSet.getTimestamp("end_time").toInstant().atOffset(java.time.ZoneOffset.UTC) : null)
                .rescheduleRequestId(resultSet.getString("reschedule_request_id"))
                .caseStage(resultSet.getString("case_stage"))
                .auditDetails(AuditDetails.builder()
                        .createdBy(resultSet.getString("created_by"))
                        .createdTime(getOffsetDateTime(resultSet.getTimestamp("created_time")))
                        .lastModifiedBy(resultSet.getString("last_modified_by"))
                        .lastModifiedTime(getOffsetDateTime(resultSet.getTimestamp("last_modified_time")))
                        .build())
                .rowVersion(resultSet.getInt("row_version")).build();
        return hearing;
    }

    private OffsetDateTime getOffsetDateTime(Timestamp timestamp) {
        return timestamp != null ? dateUtil.timestampToOffsetDateTime(timestamp) : null;
    }
}
