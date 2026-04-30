package digit.repository.rowmapper;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import digit.util.DateUtil;
import digit.web.models.AuditDetails;
import digit.web.models.OptOut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;

@Component
public class RescheduleRequestOptOutRowMapper implements RowMapper<OptOut> {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DateUtil dateUtil;

    @Override
    public OptOut mapRow(ResultSet rs, int rowNum) throws SQLException {
        try {
            return OptOut.builder()
                    .id(rs.getString("id"))
                    .judgeId(rs.getString("judge_id"))
                    .caseId(rs.getString("case_id"))
                    .rescheduleRequestId(rs.getString("reschedule_request_id"))
                    .individualId(rs.getString("individual_id"))
                    .optoutDates(rs.getString("opt_out_dates") == null ? null : objectMapper.readValue(rs.getString("opt_out_dates"), new TypeReference<List<Long>>() {
                    }))
                    .rowVersion(rs.getInt("row_version"))
                    .auditDetails(AuditDetails.builder()
                            .createdBy(rs.getString("created_by"))
                            .createdTime(getOffsetDateTime(rs.getTimestamp("created_time")))
                            .lastModifiedBy(rs.getString("last_modified_by"))
                            .lastModifiedTime(getOffsetDateTime(rs.getTimestamp("last_modified_time")))
                            .build())
                    .tenantId(rs.getString("tenant_id"))
                    .build();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private OffsetDateTime getOffsetDateTime(Timestamp timestamp) {
        return timestamp != null ? dateUtil.timestampToOffsetDateTime(timestamp) : null;
    }
}
