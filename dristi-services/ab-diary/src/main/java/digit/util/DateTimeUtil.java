package digit.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Component
public final class DateTimeUtil {

    private final ZoneId configuredZoneId;

    public DateTimeUtil(@Value("${app.zone.id}") ZoneId zoneId) {
        this.configuredZoneId = zoneId;
    }

    public ZoneId getConfiguredZoneId() {
        return configuredZoneId;
    }

    // ============================================================
    // NEW METHODS (use OffsetDateTime - preferred)
    // ============================================================

    /**
     * Parses a date string with the given pattern and returns an OffsetDateTime.
     * Supports both LocalDateTime and LocalDate formats.
     */
    public OffsetDateTime parseToOffsetDateTime(String date, String pattern) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        try {
            LocalDateTime dateTime = LocalDateTime.parse(date, formatter);
            return dateTime.atZone(configuredZoneId).toOffsetDateTime();
        } catch (DateTimeParseException localDateTimeEx) {
            try {
                LocalDate localDate = LocalDate.parse(date, formatter);
                return localDate.atStartOfDay(configuredZoneId).toOffsetDateTime();
            } catch (DateTimeParseException localDateEx) {
                DateTimeParseException combinedEx = new DateTimeParseException(
                    "Failed to parse date '" + date + "' with pattern '" + pattern + "'. " +
                    "LocalDateTime parsing failed: " + localDateTimeEx.getMessage() + ". " +
                    "LocalDate parsing failed: " + localDateEx.getMessage(),
                    date, localDateTimeEx.getErrorIndex());

                combinedEx.addSuppressed(localDateTimeEx);
                combinedEx.addSuppressed(localDateEx);

                throw combinedEx;
            }
        }
    }

    /**
     * Returns an OffsetDateTime representing the start of the given day
     * using the configured timezone.
     */
    public OffsetDateTime startOfDayOffsetDateTime(LocalDate date) {
        return date.atStartOfDay(configuredZoneId).toOffsetDateTime();
    }

    /**
     * Formats an OffsetDateTime to a string pattern using the configured timezone.
     */
    public String formatOffsetDateTime(OffsetDateTime offsetDateTime, String pattern) {
        return formatOffsetDateTime(offsetDateTime, pattern, configuredZoneId);
    }

    /**
     * Formats an OffsetDateTime to a string pattern using the specified timezone.
     */
    public String formatOffsetDateTime(OffsetDateTime offsetDateTime, String pattern, ZoneId zoneId) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return offsetDateTime.atZoneSameInstant(zoneId).format(formatter);
    }

    /**
     * Converts an OffsetDateTime to epoch milliseconds.
     * Only use for external API compatibility or legacy system integration.
     */
    public long toEpochMillis(OffsetDateTime offsetDateTime) {
        return offsetDateTime.toInstant().toEpochMilli();
    }

    /**
     * Gets the current time as OffsetDateTime using the configured timezone.
     */
    public OffsetDateTime getCurrentOffsetDateTime() {
        return OffsetDateTime.now(configuredZoneId);
    }

    // ============================================================
    // DEPRECATED METHODS (use OffsetDateTime versions instead)
    // Keep for backward compatibility during migration period
    // ============================================================

    /**
     * @deprecated Use {@link #parseToOffsetDateTime(String, String)} instead
     */
    @Deprecated(since = "2026-04-21", forRemoval = true)
    public long toEpochMillis(String date, String pattern) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        try {
            LocalDateTime dateTime = LocalDateTime.parse(date, formatter);
            return dateTime.atZone(configuredZoneId).toInstant().toEpochMilli();
        } catch (DateTimeParseException localDateTimeEx) {
            try {
                LocalDate localDate = LocalDate.parse(date, formatter);
                return startOfDayEpochMillis(localDate);
            } catch (DateTimeParseException localDateEx) {
                // Create new exception with both error messages and suppress the original exceptions
                DateTimeParseException combinedEx = new DateTimeParseException(
                    "Failed to parse date '" + date + "' with pattern '" + pattern + "'. " +
                    "LocalDateTime parsing failed: " + localDateTimeEx.getMessage() + ". " +
                    "LocalDate parsing failed: " + localDateEx.getMessage(),
                    date, localDateTimeEx.getErrorIndex());
                
                // Add both original exceptions as suppressed
                combinedEx.addSuppressed(localDateTimeEx);
                combinedEx.addSuppressed(localDateEx);
                
                throw combinedEx;
            }
        }
    }

    /**
     * @deprecated Use {@link #startOfDayOffsetDateTime(LocalDate)} instead
     */
    @Deprecated(since = "2026-04-21", forRemoval = true)
    public long startOfDayEpochMillis(LocalDate date) {
        return date.atStartOfDay(configuredZoneId).toInstant().toEpochMilli();
    }

    /**
     * @deprecated Use {@link #formatOffsetDateTime(OffsetDateTime, String, ZoneId)} instead
     */
    @Deprecated(since = "2026-04-21", forRemoval = true)
    public String formatEpochMillis(long epochMillis, String pattern, ZoneId zoneId) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return ZonedDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), zoneId).format(formatter);
    }

    /**
     * @deprecated Use {@link #formatOffsetDateTime(OffsetDateTime, String)} instead
     */
    @Deprecated(since = "2026-04-21", forRemoval = true)
    public String formatEpochMillis(long epochMillis, String pattern) {
        return formatEpochMillis(epochMillis, pattern, configuredZoneId);
    }
}
