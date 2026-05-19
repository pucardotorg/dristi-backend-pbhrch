// HAND-CURATED — lifted by Phase 35 (contract-lift)
package org.pucar.dristi.common.contract.scheduler;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.pucar.dristi.common.contract.scheduler.PeriodType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CalendarSearchCriteria implements SearchCriteria {

    @JsonProperty("tenantId")
    private String tenantId;            // required field

    @JsonProperty("judgeId")
    private String judgeId;             // required field

    @JsonProperty("courtId")
    private String courtId;             // required field

    @JsonProperty("fromDate")
    private Long fromDate;

    @JsonProperty("toDate")
    private Long toDate;

    @JsonProperty("periodType")
    private PeriodType periodType;


}
