package notification.util;

import notification.config.Configuration;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;

@Component
public class DateUtil {

    private final Configuration configuration;

    public DateUtil(Configuration configuration) {
        this.configuration = configuration;
    }

    /**
     * Converts epoch milliseconds to OffsetDateTime using the configured zone
     */
    public OffsetDateTime epochToOffsetDateTime(Long epochMillis) {
        if (epochMillis == null) {
            return null;
        }
        return OffsetDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), ZoneId.of(configuration.getZoneId()));
    }

    /**
     * Converts Timestamp to OffsetDateTime using the configured zone
     */
    public OffsetDateTime timestampToOffsetDateTime(Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }
        return timestamp.toInstant().atOffset(ZoneId.of(configuration.getZoneId()).getRules().getOffset(timestamp.toInstant()));
    }

    /**
     * Gets current time as OffsetDateTime using the configured zone
     */
    public OffsetDateTime getCurrentOffsetDateTime() {
        return OffsetDateTime.now(ZoneId.of(configuration.getZoneId()));
    }

    /**
     * Converts OffsetDateTime to Timestamp for database storage
     */
    public Timestamp offsetDateTimeToTimestamp(OffsetDateTime offsetDateTime) {
        if (offsetDateTime == null) {
            return null;
        }
        return Timestamp.from(offsetDateTime.toInstant());
    }

    /**
     * Converts OffsetDateTime to epoch milliseconds
     */
    public Long offsetDateTimeToEpoch(OffsetDateTime offsetDateTime) {
        if (offsetDateTime == null) {
            return null;
        }
        return offsetDateTime.toInstant().toEpochMilli();
    }
}
