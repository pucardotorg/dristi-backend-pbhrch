package pucar.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

import java.time.OffsetDateTime;

@Validated
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BotdOrderSummary {

    @JsonProperty("orderNumber")
    private String orderNumber = null;

    @JsonProperty("orderTitle")
    private String orderTitle = null;

    @JsonProperty("orderType")
    private String orderType = null;

    @JsonProperty("orderCategory")
    private String orderCategory = null;

    @JsonProperty("status")
    private String status = null;

    @JsonProperty("createdDate")
    private OffsetDateTime createdDate = null;

    @JsonProperty("tenantId")
    private String tenantId = null;

    @JsonProperty("filingNumber")
    private String filingNumber = null;

    @JsonProperty("hearingNumber")
    private String hearingNumber = null;

    @JsonProperty("hearingType")
    private String hearingType = null;

    @JsonProperty("itemText")
    private String itemText = null;

    @JsonProperty("purposeOfNextHearing")
    private String purposeOfNextHearing = null;

    @JsonProperty("nextHearingDate")
    private OffsetDateTime nextHearingDate = null;

    @JsonProperty("businessOfTheDay")
    private String businessOfTheDay = null;
}
