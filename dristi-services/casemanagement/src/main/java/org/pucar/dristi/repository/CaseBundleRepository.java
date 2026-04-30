package org.pucar.dristi.repository;


import org.pucar.dristi.web.models.BulkCaseBundleTracker;
import org.pucar.dristi.web.models.CaseBundleTracker;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;

@Repository
public class CaseBundleRepository {

    private final JdbcTemplate writerJdbcTemplate;

    @Autowired
    public CaseBundleRepository(@Qualifier("writerJdbcTemplate") JdbcTemplate writerJdbcTemplate) {
        this.writerJdbcTemplate = writerJdbcTemplate;
    }
    public void insertCaseTracker(CaseBundleTracker caseBundleTracker) {
        String sql = "INSERT INTO case_bundle_tracker (id, startTime, endTime, pageCount, createdBy, lastModifiedBy, createdTime, lastModifiedTime) " +
                "VALUES (?, ?, ?, ?, ?, ?,  ?, ?)";

        writerJdbcTemplate.update(sql,
                caseBundleTracker.getId(),
                caseBundleTracker.getStartTime() != null ? java.sql.Timestamp.from(caseBundleTracker.getStartTime().toInstant()) : null,
                caseBundleTracker.getEndTime() != null ? java.sql.Timestamp.from(caseBundleTracker.getEndTime().toInstant()) : null,
                caseBundleTracker.getPageCount(), caseBundleTracker.getAuditDetails().getCreatedBy(), caseBundleTracker.getAuditDetails().getLastModifiedBy()
                , caseBundleTracker.getAuditDetails().getCreatedTime(), caseBundleTracker.getAuditDetails().getLastModifiedTime());
    }

    public void insertBulkCaseTracker(BulkCaseBundleTracker bulkCaseBundleTracker) {
        String sql = "INSERT INTO case_bundle_bulk_tracker (id, startTime, endTime, caseCount) " +
                "VALUES (?, ?, ?, ?)";

        writerJdbcTemplate.update(sql,
                bulkCaseBundleTracker.getId(),
                bulkCaseBundleTracker.getStartTime() != null ? java.sql.Timestamp.from(bulkCaseBundleTracker.getStartTime().toInstant()) : null,
                bulkCaseBundleTracker.getEndTime() != null ? java.sql.Timestamp.from(bulkCaseBundleTracker.getEndTime().toInstant()) : null,
                bulkCaseBundleTracker.getCaseCount());
    }

}
