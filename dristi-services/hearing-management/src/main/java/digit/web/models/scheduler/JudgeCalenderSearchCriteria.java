package digit.web.models.scheduler;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.egov.common.contract.models.AuditDetails;
import org.springframework.validation.annotation.Validated;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;

@Validated
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class JudgeCalenderSearchCriteria {

    @JsonProperty("ruleType")
    private List<String> ruleType;

    @JsonProperty("judgeId")
    private String judgeId;

    @JsonProperty("courtId")
    private String courtId;

    @JsonProperty("fromDate")
    private OffsetDateTime fromDate;

    @JsonProperty("toDate")
    private OffsetDateTime toDate;

    @JsonProperty("tenantId")
    private String tenantId;


}