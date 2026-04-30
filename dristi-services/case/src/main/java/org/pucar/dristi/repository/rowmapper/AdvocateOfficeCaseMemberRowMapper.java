package org.pucar.dristi.repository.rowmapper;

import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.models.AuditDetails;
import org.egov.tracer.model.CustomException;
import org.pucar.dristi.web.models.advocateofficemember.AdvocateOfficeCaseMember;
import org.pucar.dristi.web.models.enums.MemberType;
import org.pucar.dristi.util.DateUtil;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;

@Component
@Slf4j
public class AdvocateOfficeCaseMemberRowMapper implements ResultSetExtractor<List<AdvocateOfficeCaseMember>> {

    private final DateUtil dateUtil;
    
    @Autowired
    public AdvocateOfficeCaseMemberRowMapper(DateUtil dateUtil) {
        this.dateUtil = dateUtil;
    }

    @Override
    public List<AdvocateOfficeCaseMember> extractData(ResultSet rs) {
        List<AdvocateOfficeCaseMember> rows = new ArrayList<>();
        try {
            while (rs.next()) {
                Timestamp lastModifiedTimeTs = rs.getTimestamp("last_modified_time");
                Timestamp createdTimeTs = rs.getTimestamp("created_time");

                AuditDetails auditDetails = AuditDetails.builder()
                        .createdBy(rs.getString("created_by"))
                        .createdTime(createdTimeTs != null ? createdTimeTs.getTime() : null)
                        .lastModifiedBy(rs.getString("last_modified_by"))
                        .lastModifiedTime(lastModifiedTimeTs != null ? lastModifiedTimeTs.getTime() : null)
                        .build();

                AdvocateOfficeCaseMember row = AdvocateOfficeCaseMember.builder()
                        .id(UUID.fromString(rs.getString("id")))
                        .tenantId(rs.getString("tenant_id"))
                        .officeAdvocateId(UUID.fromString(rs.getString("office_advocate_id")))
                        .officeAdvocateName(rs.getString("office_advocate_name"))
                        .officeAdvocateUserUuid(rs.getString("office_advocate_user_uuid"))
                        .caseId(UUID.fromString(rs.getString("case_id")))
                        .memberId(UUID.fromString(rs.getString("member_id")))
                        .memberUserUuid(rs.getString("member_user_uuid"))
                        .memberType(MemberType.valueOf(rs.getString("member_type")))
                        .memberName(rs.getString("member_name"))
                        .isActive(rs.getBoolean("is_active"))
                        .auditDetails(auditDetails)
                        .build();

                rows.add(row);
            }
            return rows;
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error occurred while processing AdvocateOfficeCaseMember ResultSet :: {}", e.toString());
            throw new CustomException("ROW_MAPPER_EXCEPTION", "Exception occurred while processing AdvocateOfficeCaseMember ResultSet: " + e.getMessage());
        }
    }
}
