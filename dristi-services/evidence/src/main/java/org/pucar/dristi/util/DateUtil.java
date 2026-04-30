package org.pucar.dristi.util;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pucar.dristi.config.Configuration;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import static org.pucar.dristi.config.ServiceConstants.DATE_PATTERN;

@Component
@Slf4j
@AllArgsConstructor
public class DateUtil {

    private final Configuration config;

    // ============================================================
    // NEW METHODS (use OffsetDateTime - preferred)
    // ============================================================

    public String getFormattedCurrentDate() {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(DATE_PATTERN);
        ZoneId zoneId = ZoneId.of(config.getZoneId());
        LocalDate currentDate = LocalDate.now(zoneId);
        return currentDate.format(dateFormatter);
    }

    /**
     * Gets the current time as OffsetDateTime using the configured timezone.
     */
    public OffsetDateTime getCurrentOffsetDateTime() {
        return OffsetDateTime.now(ZoneId.of(config.getZoneId()));
    }

    /**
     * Returns the start of the day for the given OffsetDateTime using the configured timezone.
     */
    public OffsetDateTime getStartOfDayOffsetDateTime(OffsetDateTime offsetDateTime) {
        ZoneId zoneId = ZoneId.of(config.getZoneId());
        LocalDate localDate = offsetDateTime.atZoneSameInstant(zoneId).toLocalDate();
        return localDate.atStartOfDay(zoneId).toOffsetDateTime();
    }

    /**
     * Converts a LocalDate to an OffsetDateTime representing the start of that day
     * using the configured timezone.
     */
    public OffsetDateTime getOffsetDateTimeFromLocalDate(LocalDate date) {
        ZoneId zoneId = ZoneId.of(config.getZoneId());
        return date.atStartOfDay(zoneId).toOffsetDateTime();
    }

    /**
     * Converts an OffsetDateTime to LocalDate using the configured timezone.
     */
    public LocalDate getLocalDateFromOffsetDateTime(OffsetDateTime offsetDateTime) {
        ZoneId zoneId = ZoneId.of(config.getZoneId());
        return offsetDateTime.atZoneSameInstant(zoneId).toLocalDate();
    }

    /**
     * Converts epoch milliseconds to OffsetDateTime using the configured timezone.
     * Only use this when migrating legacy epoch data.
     */
    public OffsetDateTime getOffsetDateTimeFromEpochMillis(long epochMillis) {
        return Instant.ofEpochMilli(epochMillis)
                .atZone(ZoneId.of(config.getZoneId()))
                .toOffsetDateTime();
    }

    /**
     * Converts OffsetDateTime to epoch milliseconds.
     * Only use this for external API compatibility or legacy system integration.
     */
    public Long getEpochMillisFromOffsetDateTime(OffsetDateTime offsetDateTime) {
        if (offsetDateTime == null) {
            return null;
        }
        return offsetDateTime.toInstant().toEpochMilli();
    }

    /**
     * Formats an OffsetDateTime to a string pattern using the configured timezone.
     */
    public String getFormattedDateFromOffsetDateTime(OffsetDateTime offsetDateTime, String pattern) {
        ZoneId zoneId = ZoneId.of(config.getZoneId());
        LocalDate date = offsetDateTime.atZoneSameInstant(zoneId).toLocalDate();
        return date.format(DateTimeFormatter.ofPattern(pattern));
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
     * Convert Timestamp from JDBC to OffsetDateTime using the configured timezone
     */
    public OffsetDateTime timestampToOffsetDateTime(Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }
        return OffsetDateTime.ofInstant(timestamp.toInstant(), ZoneId.of(config.getZoneId()));
    }

    // ============================================================
    // DEPRECATED METHODS (use OffsetDateTime versions instead)
    // ============================================================

    /**
     * @deprecated Use {@link #getStartOfDayOffsetDateTime(OffsetDateTime)} instead
     */
    @Deprecated(since = "2026-04-27", forRemoval = true)
    public Long getStartOfTheDayForEpoch(Long date) {
        LocalDate localDate = getLocalDateFromEpoch(date);
        return getEPochFromLocalDate(localDate);
    }

    /**
     * @deprecated Use {@link #getCurrentOffsetDateTime()} instead
     */
    @Deprecated(since = "2026-04-27", forRemoval = true)
    public Long getCurrentTimeInMilis() {
        return ZonedDateTime.now(ZoneId.of(config.getZoneId())).toInstant().toEpochMilli();
    }

    /**
     * @deprecated Use {@link #getOffsetDateTimeFromEpochMillis(long)} instead
     */
    @Deprecated(since = "2026-04-27", forRemoval = true)
    public LocalDate getLocalDateFromEpoch(long startTime) {
        return Instant.ofEpochMilli(startTime)
                .atZone(ZoneId.of(config.getZoneId()))
                .toLocalDate();
    }

    /**
     * @deprecated Use {@link #getOffsetDateTimeFromLocalDate(LocalDate)} instead
     */
    @Deprecated(since = "2026-04-27", forRemoval = true)
    public Long getEPochFromLocalDate(LocalDate date) {
        return date.atStartOfDay(ZoneId.of(config.getZoneId())).toInstant().toEpochMilli();
    }
}
