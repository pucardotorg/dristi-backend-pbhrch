package digit.util;

import digit.config.Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.UUID;

@Component
public class InPortalSurveyUtil {

    private final Configuration config;

    @Autowired
    public InPortalSurveyUtil(Configuration config) {
        this.config = config;
    }

    public OffsetDateTime getCurrentTimeOffset() {
        return OffsetDateTime.now(java.time.ZoneOffset.UTC);
    }

    public long getCurrentTimeInMilliSec() {
        return java.time.Instant.now().toEpochMilli();
    }

    public OffsetDateTime getExpiryTimeOffset(Long noOfDaysInMilliSec) {
        return getCurrentTimeOffset().plus(java.time.Duration.ofMillis(noOfDaysInMilliSec));
    }

    public UUID generateUUID() {
        return UUID.randomUUID();
    }

}
