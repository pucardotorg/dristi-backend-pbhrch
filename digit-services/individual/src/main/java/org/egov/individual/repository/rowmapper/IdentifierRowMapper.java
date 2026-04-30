package org.egov.individual.repository.rowmapper;

import digit.models.coremodels.AuditDetails;
import org.egov.common.models.individual.Identifier;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;

public class IdentifierRowMapper implements RowMapper<Identifier> {
    @Override
    public Identifier mapRow(ResultSet resultSet, int i) throws SQLException {
        return Identifier.builder()
                .id(resultSet.getString("id"))
                .individualId(resultSet.getString("individualId"))
                .clientReferenceId(resultSet.getString("clientReferenceId"))
                .identifierType(resultSet.getString("identifierType"))
                .identifierId(resultSet.getString("identifierId"))
                .auditDetails(AuditDetails.builder().createdBy(resultSet.getString("createdBy"))
                        .lastModifiedBy(resultSet.getString("lastModifiedBy"))
                        .createdTime(resultSet.getLong("createdTime"))
                        .lastModifiedTime(resultSet.getLong("lastModifiedTime")).build())
                .isDeleted(resultSet.getBoolean("isDeleted"))
                .build();

    }


    private OffsetDateTime convertToOffsetDateTime(Long epochMillis) {
        if (epochMillis == null || epochMillis == 0) {
            return null;
        }
        return OffsetDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), ZoneId.systemDefault());
    }
}