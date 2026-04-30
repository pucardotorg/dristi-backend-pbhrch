package digit.validator;

import digit.util.DateUtil;
import digit.web.models.*;
import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReScheduleRequestValidatorTest {

    @InjectMocks
    private ReScheduleRequestValidator validator;

    @Mock
    private DateUtil dateUtil;

    private BulkRescheduleRequest request;
    private BulkReschedule buklRescheduling;
    @BeforeEach
    void setUp() {
        request = new BulkRescheduleRequest();
        buklRescheduling = new BulkReschedule();
        buklRescheduling.setJudgeId("judgeId");

        buklRescheduling.setTenantId("tenantId");
        buklRescheduling.setScheduleAfter(OffsetDateTime.of(2024, 8, 1, 12, 0, 0, 0, ZoneOffset.UTC));

        request.setRequestInfo(new RequestInfo());
    }
}
