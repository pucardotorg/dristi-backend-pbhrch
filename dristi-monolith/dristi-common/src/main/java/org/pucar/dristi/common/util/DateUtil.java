// HAND-CURATED — do not regenerate
package org.pucar.dristi.common.util;

import lombok.extern.slf4j.Slf4j;
import org.pucar.dristi.common.config.CommonConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Date/time helpers that DRISTI services duplicated locally.
 *
 * <p>Two near-identical helpers existed across services with subtly
 * different semantics — both names are kept here so callers don't need
 * to change behaviour:
 * <ul>
 *   <li>{@link #getEPochFromLocalDate} — uses {@code atStartOfDay} (00:00);
 *       used by order.</li>
 *   <li>{@link #getEpochFromLocalDate} — uses {@code LocalTime.now()}
 *       (current wall-clock); used by case.</li>
 * </ul>
 *
 * <p>Zone source is {@link CommonConfiguration#getZoneId()} which reads
 * {@code app.zone.id} (default {@code Asia/Kolkata}).
 */
@Slf4j
@Component("commonDateUtil")
public class DateUtil {

    @Autowired
    private CommonConfiguration configuration;

    public Instant getInstantFrom(String time) {
        LocalTime localTime = LocalTime.parse(time);
        ZoneId zoneId = ZoneId.of(configuration.getZoneId());
        return ZonedDateTime.now(zoneId).with(localTime).toInstant();
    }

    /** Order's variant — epoch at start-of-day (00:00:00). */
    public Long getEPochFromLocalDate(LocalDate date) {
        return date.atStartOfDay(ZoneId.of(configuration.getZoneId()))
                .toInstant()
                .toEpochMilli();
    }

    /** Case's variant — epoch at current wall-clock time. */
    public Long getEpochFromLocalDate(LocalDate date) {
        return date.atTime(LocalTime.now())
                .atZone(ZoneId.of(configuration.getZoneId()))
                .toInstant()
                .toEpochMilli();
    }

    public LocalDate getLocalDateFromEpoch(long startTime) {
        return Instant.ofEpochMilli(startTime)
                .atZone(ZoneId.of(configuration.getZoneId()))
                .toLocalDate();
    }

    public String getFormattedDateFromEpoch(Long epoch, String pattern) {
        return Instant.ofEpochMilli(epoch)
                .atZone(ZoneId.of(configuration.getZoneId()))
                .toLocalDate()
                .format(DateTimeFormatter.ofPattern(pattern));
    }
}
