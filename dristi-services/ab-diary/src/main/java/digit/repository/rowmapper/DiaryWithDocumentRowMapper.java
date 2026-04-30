package digit.repository.rowmapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import digit.web.models.AuditDetails;
import digit.web.models.CaseDiary;
import digit.web.models.CaseDiaryDocument;
import lombok.extern.slf4j.Slf4j;
import org.egov.tracer.model.CustomException;
import org.postgresql.util.PGobject;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;

import static digit.config.ServiceConstants.ROW_MAPPER_EXCEPTION;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;

@Component
@Slf4j
public class DiaryWithDocumentRowMapper implements ResultSetExtractor<List<CaseDiary>> {

    private final ObjectMapper objectMapper;

    public DiaryWithDocumentRowMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public List<CaseDiary> extractData(ResultSet rs) {

        Map<UUID, CaseDiary> caseDiaryMap = new HashMap<>();

        try {
            while (rs.next()) {
                UUID diaryId = UUID.fromString(rs.getString("id"));

                CaseDiary caseDiary = caseDiaryMap.computeIfAbsent(diaryId, id -> {
                    try {
                        CaseDiary newCaseDiary = CaseDiary.builder()
                                .id(id)
                                .tenantId(rs.getString("tenantId"))
                                .caseNumber(rs.getString("caseNumber"))
                                .diaryDate(rs.getTimestamp("diaryDate") != null ? rs.getTimestamp("diaryDate").toInstant().atOffset(java.time.ZoneOffset.UTC) : null)
                                .diaryType(rs.getString("diaryType"))
                                .courtId(rs.getString("courtId"))
                                .documents(new ArrayList<>())
                                .auditDetails(AuditDetails.builder()
                                        .createdBy(rs.getString("diaryCreateBy"))
                                        .createdTime(getOffsetDateTime(rs.getTimestamp("diaryCreatedTime")))
                                        .lastModifiedBy(rs.getString("diaryLastModifiedBy"))
                                        .lastModifiedTime(getOffsetDateTime(rs.getTimestamp("diaryLastModifiedTime")))
                                        .build())
                                .build();

                        PGobject pGobject = (PGobject) rs.getObject("additionalDetails");
                        if (pGobject != null) {
                            newCaseDiary.setAdditionalDetails(objectMapper.readTree(pGobject.getValue()));
                        }
                        return newCaseDiary;
                    } catch (Exception e) {
                        throw new RuntimeException("Error mapping case diary", e);
                    }
                });

                caseDiary.getDocuments().add(
                        CaseDiaryDocument.builder()
                                .id(UUID.fromString(rs.getString("documentId")))
                                .tenantId(rs.getString("tenantId"))
                                .fileStoreId(rs.getString("fileStoreId"))
                                .documentUid(rs.getString("documentUid"))
                                .documentName(rs.getString("documentName"))
                                .documentType(rs.getString("documentType"))
                                .caseDiaryId(rs.getString("caseDiaryId"))
                                .isActive(rs.getBoolean("documentIsActive"))
                                .auditDetails(AuditDetails.builder()
                                        .createdBy(rs.getString("documentCreatedBy"))
                                        .createdTime(getOffsetDateTime(rs.getTimestamp("documentCreatedTime")))
                                        .lastModifiedBy(rs.getString("documentLastModifiedBy"))
                                        .lastModifiedTime(getOffsetDateTime(rs.getTimestamp("documentLastModifiedTime")))
                                        .build())
                                .build()
                );
            }
            return new ArrayList<>(caseDiaryMap.values());

        } catch (Exception e) {
            log.error("Error occurred while processing document ResultSet: {}", e.getMessage());
            throw new CustomException(ROW_MAPPER_EXCEPTION, "Error occurred while processing document ResultSet: " + e.getMessage());
        }
    }

    private OffsetDateTime getOffsetDateTime(Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }
        return timestamp.toInstant().atOffset(ZoneOffset.UTC);
    }
}
