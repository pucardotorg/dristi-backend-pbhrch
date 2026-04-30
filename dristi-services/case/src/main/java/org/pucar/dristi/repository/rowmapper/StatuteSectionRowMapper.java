package org.pucar.dristi.repository.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.UUID;

import org.egov.common.contract.models.AuditDetails;
import org.egov.tracer.model.CustomException;
import org.postgresql.util.PGobject;
import org.pucar.dristi.web.models.StatuteSection;
import org.pucar.dristi.util.DateUtil;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;

@Component
@Slf4j
public class StatuteSectionRowMapper implements ResultSetExtractor<Map<UUID, List<StatuteSection>>> {
    
    private final DateUtil dateUtil;
    
    @Autowired
    public StatuteSectionRowMapper(DateUtil dateUtil) {
        this.dateUtil = dateUtil;
    }
    public Map<UUID, List<StatuteSection>> extractData(ResultSet rs) {
        Map<UUID, List<StatuteSection>> statuteSectionMap = new LinkedHashMap<>();

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            while (rs.next()) {
                String id = rs.getString("case_id");
                UUID uuid = UUID.fromString(id!=null ? id : "00000000-0000-0000-0000-000000000000");

                Timestamp lastModifiedTimeTs = rs.getTimestamp("lastmodifiedtime");
                Timestamp createdTimeTs = rs.getTimestamp("createdtime");

                AuditDetails auditdetails = AuditDetails.builder()
                        .createdBy(rs.getString("createdby"))
                        .createdTime(createdTimeTs != null ? createdTimeTs.getTime() : null)
                        .lastModifiedBy(rs.getString("lastmodifiedby"))
                        .lastModifiedTime(lastModifiedTimeTs != null ? lastModifiedTimeTs.getTime() : null)
                        .build();
                StatuteSection statuteSection = StatuteSection.builder()
                        .id(UUID.fromString(rs.getString("id")))
                        .tenantId(rs.getString("tenantid"))
                        .sections(stringToList(rs.getString("sections")))
                        .subsections(stringToList(rs.getString("subsections")))
                        .statute(rs.getString("statutes"))
                        .auditdetails(auditdetails)
                        .build();

                PGobject pgObject = (PGobject) rs.getObject("additionalDetails");
                if (pgObject != null)
                    statuteSection.setAdditionalDetails(objectMapper.readTree(pgObject.getValue()));

                if (statuteSectionMap.containsKey(uuid) ) {
                    statuteSectionMap.get(uuid).add(statuteSection);
                }
                else{
                    List<StatuteSection> statuteSections = new ArrayList<>();
                    statuteSections.add(statuteSection);
                    statuteSectionMap.put(uuid,statuteSections);
                }
            }
        } catch(CustomException e){
            throw e;
        } catch (Exception e) {
            log.error("Error occurred while processing Case ResultSet :: {}", e.toString());
            throw new CustomException("ROW_MAPPER_EXCEPTION", "Exception occurred while processing Case ResultSet: " + e.getMessage());
        }
        return statuteSectionMap;
    }

    public List<String> stringToList(String str){
        List<String> list = new ArrayList<>();
        if(str!=null){
            StringTokenizer st = new StringTokenizer(str,",");
            while (st.hasMoreTokens()) {
                list.add(st.nextToken());
            }
        }

        return list;
    }

}
