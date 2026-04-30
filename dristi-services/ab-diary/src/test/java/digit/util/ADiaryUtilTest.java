package digit.util;

import digit.config.Configuration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class ADiaryUtilTest {

    @Mock
    private Configuration config;

    @InjectMocks
    private ADiaryUtil aDiaryUtil;

    @Test
    void testGetCurrentTimeOffset() {
        OffsetDateTime before = OffsetDateTime.now().minusSeconds(1);
        OffsetDateTime currentTime = aDiaryUtil.getCurrentTimeOffset();
        OffsetDateTime after = OffsetDateTime.now().plusSeconds(1);

        assertNotNull(currentTime);
        assertTrue(currentTime.isAfter(before));
        assertTrue(currentTime.isBefore(after));
    }

    @Test
    void testGenerateUUID() {
        UUID uuid = aDiaryUtil.generateUUID();

        assertNotNull(uuid);

        assertDoesNotThrow(() -> UUID.fromString(uuid.toString()));

        UUID anotherUuid = aDiaryUtil.generateUUID();
        assertNotNull(anotherUuid);
        assertNotEquals(uuid, anotherUuid);
    }
}
