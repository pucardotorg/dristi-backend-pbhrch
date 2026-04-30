package org.egov.transformer.util;

import lombok.AllArgsConstructor;
import org.egov.transformer.config.TransformerProperties;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Component
@AllArgsConstructor
public class DateUtil {

    private final TransformerProperties properties;

    private ZoneId getConfiguredZoneId() {
        return ZoneId.of(properties.getApplicationZoneId());
    }


    /**
     * @deprecated Use {@link #getOffsetDateTimeFromEpoch(long)} and convert as needed
     */
    @Deprecated(since = "2026-04-21", forRemoval = true)
    public LocalDateTime getLocalDateTimeFromEpoch(long startTime) {
        if (startTime < 0) {
            throw new IllegalArgumentException("Start time cannot be negative");
        }
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(startTime), ZoneId.of(properties.getApplicationZoneId()));
    }

    /**
     * Get OffsetDateTime from epoch milliseconds using configured timezone.
     * For legacy/external API use only. Prefer using OffsetDateTime directly.
     */
    public OffsetDateTime getOffsetDateTimeFromEpoch(long epochMillis) {
        if (epochMillis < 0) {
            throw new IllegalArgumentException("Epoch time cannot be negative");
        }
        return OffsetDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), getConfiguredZoneId());
    }

    public LocalTime getLocalTime(String time, String pattern) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return LocalTime.parse(time, formatter);
    }

    public LocalTime getLocalTime(String time) {
        return getLocalTime(time, "HH:mm:ss");
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
        if (startTime < 0) {
            throw new IllegalArgumentException("Start time cannot be negative");
        }
        return Instant.ofEpochMilli(startTime)
                .atZone(ZoneId.of(properties.getApplicationZoneId()))
                .toLocalDate();
    }

    /**
     * Get LocalDate from OffsetDateTime.
     */
    public LocalDate getLocalDateFromOffsetDateTime(OffsetDateTime offsetDateTime) {
        return offsetDateTime.atZoneSameInstant(getConfiguredZoneId()).toLocalDate();
    }

    /**
     * Get OffsetDateTime from LocalDate using configured timezone.
     */
    public OffsetDateTime getOffsetDateTimeFromLocalDate(LocalDate date) {
        return date.atStartOfDay(getConfiguredZoneId()).toOffsetDateTime();
    }

    /**
     * @deprecated Use {@link #getOffsetDateTimeFromLocalDate(LocalDate)} and convert to epoch only for external APIs
     */
    @Deprecated(since = "2026-04-21", forRemoval = true)
    public Long getEpochFromLocalDate(LocalDate date) {
        return date.atStartOfDay(ZoneId.of(properties.getApplicationZoneId())).toInstant().toEpochMilli();
    }

    /**
     * @deprecated Use OffsetDateTime throughout; only convert to epoch for external APIs
     */
    @Deprecated(since = "2026-04-21", forRemoval = true)
    public Long getEpochFromLocalDateTime(LocalDateTime dateTime) {
        return dateTime.atZone(ZoneId.of(properties.getApplicationZoneId())).toInstant().toEpochMilli();
    }

    /**
     * @deprecated Use OffsetDateTime methods instead
     */
    @Deprecated(since = "2026-04-21", forRemoval = true)
    public Long getStartOfTheDayForEpoch(Long date) {
        LocalDate localDate = getLocalDateFromEpoch(date);
        return getEpochFromLocalDate(localDate);
    }

    /**
     * @deprecated Use {@link #getCurrentOffsetDateTime()}
     */
    @Deprecated(since = "2026-04-21", forRemoval = true)
    public Long getCurrentTimeInMillis() {
        return ZonedDateTime.now(ZoneId.of(properties.getApplicationZoneId())).toInstant().toEpochMilli();
    }

    /**
     * Parse date string to OffsetDateTime using configured timezone.
     */
    public OffsetDateTime parseToOffsetDateTime(String date, String pattern) {
        DateTimeFormatter format = DateTimeFormatter.ofPattern(pattern);
        LocalDate localDate = LocalDate.parse(date, format);
        return localDate.atStartOfDay(getConfiguredZoneId()).toOffsetDateTime();
    }

    /**
     * @deprecated Use {@link #parseToOffsetDateTime(String, String)}
     */
    @Deprecated(since = "2026-04-21", forRemoval = true)
    public Long getEpochFromDateString(String date, String formatter) {
        DateTimeFormatter format = DateTimeFormatter.ofPattern(formatter);
        LocalDate localDate = LocalDate.parse(date, format);
        return localDate.atStartOfDay(ZoneId.of(properties.getApplicationZoneId())).toInstant().toEpochMilli();
    }

    /**
     * @deprecated Use {@link #getLocalDateFromOffsetDateTime(OffsetDateTime)} and getYear()
     */
    @Deprecated(since = "2026-04-21", forRemoval = true)
    public String getYearFromDate(Long date) {
        if (date == null) return null;
        LocalDate localDate = getLocalDateFromEpoch(date);
        return String.valueOf(localDate.getYear());
    }

    /**
     * Get year from OffsetDateTime.
     */
    public String getYearFromOffsetDateTime(OffsetDateTime offsetDateTime) {
        if (offsetDateTime == null) return null;
        return String.valueOf(offsetDateTime.getYear());
    }

    /**
     * Get current time as OffsetDateTime using MDMS-configured timezone
     */
    public OffsetDateTime getCurrentOffsetDateTime() {
        return OffsetDateTime.now(getConfiguredZoneId());
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
        return OffsetDateTime.ofInstant(timestamp.toInstant(), getConfiguredZoneId());
    }
}
