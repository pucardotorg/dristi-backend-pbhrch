package org.egov.pgr.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;

@Component
@Slf4j
public class DateUtil {

    private final ZoneId zoneId;

    @Autowired
    public DateUtil(@Value("${app.timezone}") String timeZone) {
        this.zoneId = ZoneId.of(timeZone);
    }

    /**
     * Get current OffsetDateTime based on configured zoneId
     */
    public OffsetDateTime getCurrentOffsetDateTime() {
        return OffsetDateTime.now(zoneId);
    }

    /**
     * Convert Timestamp to OffsetDateTime using configured zoneId
     */
    public OffsetDateTime timestampToOffsetDateTime(Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }
        return OffsetDateTime.ofInstant(Instant.ofEpochMilli(timestamp.getTime()), zoneId);
    }

    /**
     * Convert epoch millis to OffsetDateTime using configured zoneId
     * @deprecated Use OffsetDateTime directly instead of epoch millis
     */
    @Deprecated
    public OffsetDateTime epochMillisToOffsetDateTime(Long epochMillis) {
        if (epochMillis == null) {
            return null;
        }
        return OffsetDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), zoneId);
    }
}
