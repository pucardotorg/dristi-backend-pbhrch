package digit.util;

import digit.config.Configuration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DigitalizedDocumentUtilTest {

    @Mock
    private Configuration config;

    @InjectMocks
    private DigitalizedDocumentUtil util;

    @Test
    void basic() {
        when(config.getZoneId()).thenReturn("UTC");
        OffsetDateTime t1 = util.getCurrentTimeOffset();
        assertNotNull(t1);

        UUID id = util.generateUUID();
        assertNotNull(id);
    }
}
