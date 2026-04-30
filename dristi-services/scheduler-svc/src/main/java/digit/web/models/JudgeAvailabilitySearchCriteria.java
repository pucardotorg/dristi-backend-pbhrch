package digit.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;



@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class JudgeAvailabilitySearchCriteria implements SearchCriteria {

    @JsonProperty("tenantId")
    private String tenantId;                        //required

    @JsonProperty("judgeId")
    private String judgeId;                         //required

    @JsonProperty("courtId")
    private String courtId;                         //required

    //TODO : need to configure
    @JsonProperty("numberOfSuggestedDays")
    private Integer numberOfSuggestedDays = 5;

    @JsonProperty("fromDate")
    private OffsetDateTime fromDate;

    @JsonProperty("toDate")
    private OffsetDateTime toDate;

}
