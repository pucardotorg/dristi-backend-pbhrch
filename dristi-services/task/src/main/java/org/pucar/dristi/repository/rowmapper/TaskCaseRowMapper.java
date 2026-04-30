package org.pucar.dristi.repository.rowmapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.pucar.dristi.web.models.AuditDetails;
import org.egov.tracer.model.CustomException;
import org.postgresql.util.PGobject;
import org.pucar.dristi.web.models.AssignedTo;
import org.pucar.dristi.web.models.Task;
import org.pucar.dristi.web.models.TaskCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

import static org.pucar.dristi.config.ServiceConstants.ROW_MAPPER_EXCEPTION;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;


@Slf4j
@Component
public class TaskCaseRowMapper implements ResultSetExtractor<List<TaskCase>> {

    private final ObjectMapper objectMapper;

    @Autowired
    public TaskCaseRowMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public List<TaskCase> extractData(ResultSet rs) {
        Map<String, TaskCase> taskMap = new LinkedHashMap<>();

        try {
            while (rs.next()) {
                String uuid = rs.getString("id");
                TaskCase task = taskMap.get(uuid);

                if (task == null) {
                    Timestamp createdTs = rs.getTimestamp("createdtime");
                    Timestamp lastModifiedTs = rs.getTimestamp("lastmodifiedtime");

                    AuditDetails auditdetails = AuditDetails.builder()
                            .createdBy(rs.getString("createdby"))
                            .createdTime(createdTs != null ? createdTs.toInstant().atOffset(ZoneOffset.UTC) : null)
                            .lastModifiedBy(rs.getString("lastmodifiedby"))
                            .lastModifiedTime(lastModifiedTs != null ? lastModifiedTs.toInstant().atOffset(ZoneOffset.UTC) : null)
                            .build();

                    task = TaskCase.builder()
                            .id(UUID.fromString(rs.getString("id")))
                            .tenantId(rs.getString("tenantid"))
                            .orderId(UUID.fromString(rs.getString("orderid")))
                            .filingNumber(rs.getString("filingnumber"))
                            .taskNumber(rs.getString("tasknumber"))
                            .cnrNumber(rs.getString("cnrnumber"))
                            .cmpNumber(rs.getString("cmpNumber"))
                            .courtCaseNumber(rs.getString("courtCaseNumber"))
                            .courtId(rs.getString("courtId"))
                            .createdDate(tsToOdt(rs.getTimestamp("createddate")))
                            .dateCloseBy(tsToOdt(rs.getTimestamp("datecloseby")))
                            .dateClosed(tsToOdt(rs.getTimestamp("dateclosed")))
                            .taskDescription(rs.getString("taskdescription"))
                            .taskDetails(objectMapper.readValue(rs.getString("taskdetails"), Object.class))
                            .taskType(rs.getString("tasktype"))
                            .status(rs.getString("status"))
                            .documentStatus(rs.getString("documentStatus"))
                            .assignedTo(getObjectFromJson(rs.getString("assignedto"), new TypeReference<AssignedTo>() {
                            }))
                            .isActive(Boolean.valueOf(rs.getString("isactive")))
                            .auditDetails(auditdetails)
                            .caseName(rs.getString("casename"))
                            .orderType(rs.getString("ordertype"))
                            .build();
                }

                PGobject pgObject = (PGobject) rs.getObject("additionaldetails");
                if (pgObject != null)
                    task.setAdditionalDetails(objectMapper.readTree(pgObject.getValue()));

                taskMap.put(uuid, task);
            }
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error occurred while processing task ResultSet :: {}", e.toString());
            throw new CustomException(ROW_MAPPER_EXCEPTION, "Error occurred while processing Task ResultSet: " + e.getMessage());
        }
        return new ArrayList<>(taskMap.values());
    }

    private LocalDate stringToLocalDate(String str) {
        LocalDate localDate = null;
        if (str != null)
            try {
                DateTimeFormatter pattern = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                localDate = LocalDate.parse(str, pattern);
            } catch (DateTimeParseException e) {
                log.error("Date parsing failed for input: {}", str, e);
                throw new CustomException("DATE_PARSING_FAILED", "Failed to parse date: " + str);
            }

        return localDate;
    }

    private OffsetDateTime tsToOdt(Timestamp ts) {
        return ts != null ? ts.toInstant().atOffset(ZoneOffset.UTC) : null;
    }

    public <T> T getObjectFromJson(String json, TypeReference<T> typeRef) {
        if (json == null || json.trim().isEmpty()) {
            try {
                return objectMapper.readValue("{}", typeRef); // Return an empty object of the specified type
            } catch (IOException e) {
                throw new CustomException("Failed to create an empty instance of " + typeRef.getType(), e.getMessage());
            }
        }
        try {
            return objectMapper.readValue(json, typeRef);
        } catch (Exception e) {
            throw new CustomException("Failed to convert JSON to " + typeRef.getType(), e.getMessage());
        }
    }

}
