package digit.util;

import digit.config.Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Component
public class ADiaryUtil {

    private final Configuration config;

    @Autowired
    public ADiaryUtil(Configuration config) {
        this.config = config;
    }

    public OffsetDateTime getCurrentTimeOffset() {
        return OffsetDateTime.now(ZoneOffset.UTC);
    }

    public UUID generateUUID() {
        return UUID.randomUUID();
    }

}
