package org.pucar.dristi.repository.rowmapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.tracer.model.CustomException;
import org.postgresql.util.PGobject;
import org.pucar.dristi.web.models.Advocate;
import org.pucar.dristi.web.models.AuditDetails;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;

import static org.pucar.dristi.config.ServiceConstants.ROW_MAPPER_EXCEPTION;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;

@Component
@Slf4j
public class AdvocateRowMapper implements ResultSetExtractor<List<Advocate>> {
    
    public AdvocateRowMapper() {}

    /** To map query result to a list of advocate instance
     * @param rs
     * @return list of advocate
     */
    public List<Advocate> extractData(ResultSet rs) {
        Map<String, Advocate> advocateMap = new LinkedHashMap<>();

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            while (rs.next()) {
                String uuid = rs.getString("applicationnumber");
                Advocate advocate = advocateMap.get(uuid);

                if (advocate == null) {
                    Timestamp lastModifiedTimeTs = rs.getTimestamp("lastmodifiedtime");
                    Timestamp createdTimeTs = rs.getTimestamp("createdtime");

                    AuditDetails auditdetails = AuditDetails.builder()
                            .createdBy(rs.getString("createdby"))
                            .createdTime(createdTimeTs != null ? createdTimeTs.toInstant().atOffset(ZoneOffset.UTC) : null)
                            .lastModifiedBy(rs.getString("lastmodifiedby"))
                            .lastModifiedTime(lastModifiedTimeTs != null ? lastModifiedTimeTs.toInstant().atOffset(ZoneOffset.UTC) : null)
                            .build();

                    advocate = Advocate.builder()
                            .applicationNumber(rs.getString("applicationnumber"))
                            .tenantId(rs.getString("tenantid"))
                            .id(UUID.fromString(rs.getString("id")))
                            .barRegistrationNumber(rs.getString("barregistrationnumber"))
                            .organisationID(toUUID(rs.getString("organisationid")))
                            .individualId(rs.getString("individualid"))
                            .isActive(rs.getBoolean("isactive"))
                            .advocateType(rs.getString("advocatetype"))
                            .status(rs.getString("status"))
                            .auditDetails(auditdetails)
                            .build();
                }

                PGobject pgObject = (PGobject) rs.getObject("additionalDetails");
                if(pgObject!=null)
                    advocate.setAdditionalDetails(objectMapper.readTree(pgObject.getValue()));

                advocateMap.put(uuid, advocate);
            }
        } catch(CustomException e){
            throw e;
        }
        catch (Exception e){
            log.error("Error occurred while processing Advocate ResultSet :: {}", e.toString());
            throw new CustomException(ROW_MAPPER_EXCEPTION,"Exception occurred while processing Advocate ResultSet: "+ e.getMessage());
        }
        return new ArrayList<>(advocateMap.values());
    }
    UUID toUUID(String toUuid) {
        if(toUuid == null) {
            return null;
        }
        return UUID.fromString(toUuid);
    }



}
