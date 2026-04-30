package digit.repository.rowmapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import digit.util.DateUtil;
import digit.web.models.AuditDetails;
import digit.web.models.JudgeCalendarRule;
import digit.web.models.enums.JudgeRuleType;
import lombok.extern.slf4j.Slf4j;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;


@Component
@Slf4j
public class CalendarRowMapper implements RowMapper<JudgeCalendarRule> {
    private final ObjectMapper objectMapper;
    private final DateUtil dateUtil;

    @Autowired
    public CalendarRowMapper(ObjectMapper objectMapper, DateUtil dateUtil) {
        this.objectMapper = objectMapper;
        this.dateUtil = dateUtil;
    }

    @Override
    public JudgeCalendarRule mapRow(ResultSet resultSet, int rowNum) throws SQLException {


        JudgeCalendarRule calendar = JudgeCalendarRule.builder()
                .id(resultSet.getString("id"))
                .judgeId(resultSet.getString("judge_id"))
                .ruleType(resultSet.getString("rule_type"))
                .date(resultSet.getTimestamp("date") != null ? resultSet.getTimestamp("date").toInstant().atOffset(java.time.ZoneOffset.UTC) : null)
                .notes(resultSet.getString("notes"))
                .tenantId(resultSet.getString("tenant_id"))
                .courtIds(getListFromJson(resultSet.getString("court_ids")))
                .auditDetails(AuditDetails.builder()
                        .createdBy(resultSet.getString("created_by"))
                        .createdTime(getOffsetDateTime(resultSet.getTimestamp("created_time")))
                        .lastModifiedBy(resultSet.getString("last_modified_by"))
                        .lastModifiedTime(getOffsetDateTime(resultSet.getTimestamp("last_modified_time")))
                        .build())
                .rowVersion(resultSet.getInt("row_version"))
                .build();

        return calendar;
    }

    private OffsetDateTime getOffsetDateTime(Timestamp timestamp) {
        return timestamp != null ? dateUtil.timestampToOffsetDateTime(timestamp) : null;
    }

    public List<String> getListFromJson(String json) {
        if (json == null || json.trim().isEmpty()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            throw new CustomException("Failed to convert JSON to List<String>", e.getMessage());
        }
    }
}
