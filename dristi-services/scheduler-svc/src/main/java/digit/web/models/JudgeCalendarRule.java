package digit.web.models;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

import java.time.OffsetDateTime;
import java.util.List;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;

/**
 * JudgeCalendarRule
 */
@Validated
@jakarta.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2024-04-16T18:22:58.738027694+05:30[Asia/Kolkata]")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class JudgeCalendarRule {

    @JsonProperty("id")
    private String id;

    @JsonProperty("judgeId")
    private String judgeId;

    @JsonProperty("ruleType")
    private String ruleType;

    @JsonProperty("date")
    private OffsetDateTime date;

    @JsonProperty("notes")
    private String notes;

    @JsonProperty("tenantId")
    private String tenantId;

    @JsonProperty("courtIds")
    private List<String> courtIds;

    @JsonProperty("auditDetails")
    private AuditDetails auditDetails;

    @JsonProperty("rowVersion")
    private Integer rowVersion = null;
}
