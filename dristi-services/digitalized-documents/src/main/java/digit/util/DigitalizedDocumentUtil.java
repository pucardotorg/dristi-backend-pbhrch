package digit.util;

import digit.config.Configuration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.UUID;

@Component
@Slf4j
public class DigitalizedDocumentUtil {

    private final Configuration config;

    @Autowired
    public DigitalizedDocumentUtil(Configuration config) {
        this.config = config;
    }

    public OffsetDateTime getCurrentTimeOffset() {
        return OffsetDateTime.now(ZoneId.of(config.getZoneId()));
    }

    public UUID generateUUID() {
        return UUID.randomUUID();
    }

}
