package digit.repository.rowmapper;

import digit.web.models.CauseList;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
@Slf4j
public class CauseListRowMapper implements RowMapper<CauseList> {

    @Override
    public CauseList mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        String advocateNameString = resultSet.getString("advocate_names");
        List<String> advocateNames = new ArrayList<>();

        if (advocateNameString != null) {
            advocateNames = Arrays.asList(advocateNameString.split(","));
        }
        return CauseList.builder()
                .courtId(resultSet.getString("court_id"))
                .tenantId(resultSet.getString("tenant_id"))
                .judgeId(resultSet.getString("judge_id"))
                .hearingId(resultSet.getString("hearing_id"))
                .slot(resultSet.getString("slot"))
                .startTime(resultSet.getTimestamp("start_time") != null ? resultSet.getTimestamp("start_time").toInstant().atOffset(java.time.ZoneOffset.UTC) : null)
                .endTime(resultSet.getTimestamp("end_time") != null ? resultSet.getTimestamp("end_time").toInstant().atOffset(java.time.ZoneOffset.UTC) : null)
                .caseId(resultSet.getString("case_id"))
                .caseType(resultSet.getString("case_type"))
                .caseNumber(resultSet.getString("case_number"))
                .caseTitle(resultSet.getString("case_title"))
                .hearingDate(resultSet.getString("hearing_date"))
                .caseRegistrationDate(resultSet.getTimestamp("case_registration_date") != null ? resultSet.getTimestamp("case_registration_date").toInstant().atOffset(java.time.ZoneOffset.UTC) : null)
                .advocateNames(advocateNames)
                .build();
    }
}
