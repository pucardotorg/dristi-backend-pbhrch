package digit.util;

import digit.config.Configuration;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import static digit.config.ServiceConstants.DATE_PATTERN;

@Component
@Slf4j
@AllArgsConstructor
public class DateUtil {

    private final Configuration config;

    public String getFormattedCurrentDate() {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(DATE_PATTERN);
        ZoneId zoneId = ZoneId.of(config.getZoneId());
        LocalDate currentDate = LocalDate.now(zoneId);

        return currentDate.format(dateFormatter);
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
