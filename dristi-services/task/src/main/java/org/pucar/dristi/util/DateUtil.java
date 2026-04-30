package org.pucar.dristi.util;

import org.pucar.dristi.config.Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.*;
import java.time.format.DateTimeFormatter;

@Component
public class DateUtil {

    private final Configuration config;

    @Autowired
    public DateUtil(Configuration config) {
        this.config = config;
    }


    /**
     * @deprecated Use {@link #getOffsetDateTimeFromEpoch(long)} and convert as needed
     */
    @Deprecated(since = "2026-04-21", forRemoval = true)
    public LocalDateTime getLocalDateTimeFromEpoch(long startTime) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(startTime), ZoneId.of(config.getZoneId()));
    }

    /**
     * Get OffsetDateTime from epoch milliseconds using configured timezone.
     * For legacy/external API use only. Prefer using OffsetDateTime directly.
     */
    public OffsetDateTime getOffsetDateTimeFromEpoch(long epochMillis) {
        return OffsetDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), ZoneId.of(config.getZoneId()));
    }

    public LocalTime getLocalTime(String time) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        // Parse the time string into a LocalTime object
        return LocalTime.parse(time, formatter);
    }

    public LocalDateTime getLocalDateTime(LocalDateTime dateTime, String newTime) {

        LocalTime time = getLocalTime(newTime);

        return dateTime.with(time);

    }

    /**
     * @deprecated Use {@link #getLocalDateFromOffsetDateTime(OffsetDateTime)}
     */
    @Deprecated(since = "2026-04-21", forRemoval = true)
    public LocalDate getLocalDateFromEpoch(long startTime) {
        return Instant.ofEpochMilli(startTime)
                .atZone(ZoneId.of(config.getZoneId()))
                .toLocalDate();
    }

    /**
     * Get LocalDate from OffsetDateTime.
     */
    public LocalDate getLocalDateFromOffsetDateTime(OffsetDateTime offsetDateTime) {
        return offsetDateTime.atZoneSameInstant(ZoneId.of(config.getZoneId())).toLocalDate();
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
    public Long getEPochFromLocalDate(LocalDate date) {
        return date.atStartOfDay(ZoneId.of(config.getZoneId())).toInstant().toEpochMilli();
    }

    /**
     * @deprecated Use OffsetDateTime throughout; only convert to epoch for external APIs
     */
    @Deprecated(since = "2026-04-21", forRemoval = true)
    public Long getEpochFromLocalDateTime(LocalDateTime dateTime) {
        return dateTime.atZone(ZoneId.of(config.getZoneId())).toInstant().toEpochMilli();
    }

    /**
     * @deprecated Use OffsetDateTime methods instead
     */
    @Deprecated(since = "2026-04-21", forRemoval = true)
    public Long getStartOfTheDayForEpoch(Long date) {
        LocalDate localDate = getLocalDateFromEpoch(date);
        return getEPochFromLocalDate(localDate);
    }

    public String getCurrentDate() {
        LocalDate currentDate = getLocalDateFromOffsetDateTime(getCurrentOffsetDateTime());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyyyy");
        return currentDate.format(formatter);
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
