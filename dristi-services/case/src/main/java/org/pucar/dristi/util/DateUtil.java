package org.pucar.dristi.util;

import lombok.extern.slf4j.Slf4j;
import org.pucar.dristi.config.Configuration;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

@Slf4j
@Component
public class DateUtil {

    private final Configuration configuration;

    public DateUtil(Configuration configuration) {
        this.configuration = configuration;
    }
    /**
     * Converts a "HH:mm:ss" time string to a Date object representing that time today
     */
    public Instant getInstantFrom(String time) {
        LocalTime localTime = LocalTime.parse(time);
        ZonedDateTime currentDate = ZonedDateTime.now(ZoneId.of(configuration.getZoneId()));
        ZonedDateTime zonedDateTime = currentDate.with(localTime);

        return Date.from(zonedDateTime.toInstant()).toInstant();
    }

    /**
     * @deprecated Use {@link #getLocalDateFromOffsetDateTime(OffsetDateTime)}
     */
    @Deprecated(since = "2026-04-21", forRemoval = true)
    public LocalDate getLocalDateFromEpoch(long startTime) {
        return Instant.ofEpochMilli(startTime)
                .atZone(ZoneId.of(configuration.getZoneId()))
                .toLocalDate();
    }

    /**
     * Get LocalDate from OffsetDateTime using configured timezone.
     */
    public LocalDate getLocalDateFromOffsetDateTime(OffsetDateTime offsetDateTime) {
        return offsetDateTime.atZoneSameInstant(ZoneId.of(configuration.getZoneId())).toLocalDate();
    }

    /**
     * Get OffsetDateTime from LocalDate using configured timezone.
     */
    public OffsetDateTime getOffsetDateTimeFromLocalDate(LocalDate date) {
        return date.atStartOfDay(ZoneId.of(configuration.getZoneId())).toOffsetDateTime();
    }

    /**
     * @deprecated Use {@link #getOffsetDateTimeFromLocalDate(LocalDate)} and convert to epoch only for external APIs
     */
    @Deprecated(since = "2026-04-21", forRemoval = true)
    public Long getEpochFromLocalDate(LocalDate date) {
        return date.atTime(LocalTime.now()).atZone(ZoneId.of(configuration.getZoneId())).toInstant().toEpochMilli();
    }

    /**
     * Get current time as OffsetDateTime using MDMS-configured timezone
     */
    public OffsetDateTime getCurrentOffsetDateTime() {
        return OffsetDateTime.now(ZoneId.of(configuration.getZoneId()));
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
        return OffsetDateTime.ofInstant(timestamp.toInstant(), ZoneId.of(configuration.getZoneId()));
    }
}
