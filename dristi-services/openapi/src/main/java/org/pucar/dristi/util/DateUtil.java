package org.pucar.dristi.util;

import org.pucar.dristi.config.Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Component
public class DateUtil {

    private final Configuration config;

    @Autowired
    public DateUtil(Configuration config) {
        this.config = config;
    }
    public List<Long> getYearInSeconds(Integer year) {

        try {
            ZoneId zoneId = ZoneId.of(config.getZoneId());
            ZonedDateTime startOfYear = ZonedDateTime.of(year, 1, 1, 0, 0, 0, 0, zoneId);
            long startOfYearMillis = startOfYear.toInstant().toEpochMilli();

            ZonedDateTime endOfYear = ZonedDateTime.of(year, 12, 31, 23, 59, 59, 999_999_999, zoneId);
            long endOfYearMillis = endOfYear.toInstant().toEpochMilli();

            return List.of(startOfYearMillis, endOfYearMillis);
        }
        catch (Exception e) {
            throw new RuntimeException("Error while getting year in seconds", e);
        }
    }

    /**
     * Get OffsetDateTime range for a year as List of [start, end].
     * Replaces getYearInSeconds which returned epoch millis.
     */
    public List<OffsetDateTime> getYearAsOffsetDateTime(Integer year) {
        try {
            ZoneId zoneId = ZoneId.of(config.getZoneId());
            OffsetDateTime startOfYear = OffsetDateTime.of(year, 1, 1, 0, 0, 0, 0, zoneId.getRules().getOffset(java.time.Instant.now()));
            OffsetDateTime endOfYear = OffsetDateTime.of(year, 12, 31, 23, 59, 59, 999_999_999, zoneId.getRules().getOffset(java.time.Instant.now()));
            return List.of(startOfYear, endOfYear);
        } catch (Exception e) {
            throw new RuntimeException("Error while getting year as OffsetDateTime", e);
        }
    }

    /**
     * @deprecated Use OffsetDateTime throughout; only convert to epoch for external APIs
     */
    @Deprecated(since = "2026-04-21", forRemoval = true)
    public Long getEpochFromLocalDateTime(LocalDateTime dateTime) {
        return dateTime.atZone(ZoneId.of(config.getZoneId())).toInstant().toEpochMilli();
    }

    /**
     * Get OffsetDateTime from LocalDate using configured timezone.
     */
    public OffsetDateTime getOffsetDateTimeFromLocalDate(LocalDate date) {
        return date.atStartOfDay(ZoneId.of(config.getZoneId())).toOffsetDateTime();
    }

    /**
     * @deprecated Use {@link #getOffsetDateTimeFromLocalDate(LocalDate)} and convert to epoch only for external APIs
     */
    @Deprecated(since = "2026-04-21", forRemoval = true)
    public Long getEpochFromLocalDate(LocalDate date) {
        return date.atStartOfDay(ZoneId.of(config.getZoneId())).toInstant().toEpochMilli();
    }

    /**
     * Get current time as OffsetDateTime using MDMS-configured timezone
     */
    public OffsetDateTime getCurrentOffsetDateTime() {
        return OffsetDateTime.now(ZoneId.of(config.getZoneId()));
    }

    /**
     * Convert OffsetDateTime to Timestamp for JDBC writes
     */
    public Timestamp offsetDateTimeToTimestamp(OffsetDateTime offsetDateTime) {
        if (offsetDateTime == null) {
            return null;
        }
        return Timestamp.from(offsetDateTime.toInstant());
    }

    /**
     * Convert Timestamp from JDBC to OffsetDateTime using MDMS-configured timezone
     */
    public OffsetDateTime timestampToOffsetDateTime(Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }
        return OffsetDateTime.ofInstant(timestamp.toInstant(), ZoneId.of(config.getZoneId()));
    }
}
