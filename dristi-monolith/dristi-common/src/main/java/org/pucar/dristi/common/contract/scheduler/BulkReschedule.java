// HAND-CURATED — lifted by Phase 35 (contract-lift)
package org.pucar.dristi.common.contract.scheduler;


import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Validated
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BulkReschedule {

    @JsonProperty("judgeId")
    @NotNull
    private String judgeId;

    @JsonProperty("tenantId")
    @NotNull
    private String tenantId;

    @JsonProperty("scheduleAfter")
    private Long scheduleAfter;

    @JsonProperty("courtId")
    @NotNull
    private String courtId;

    @JsonProperty("hearingIds")
    List<String> hearingIds;

    @JsonProperty("startTime")
    private Long startTime;

    @JsonProperty("endTime")
    private Long endTime;

    @JsonProperty("slotIds")
    private Set<Integer> slotIds = new HashSet<>();

    @JsonProperty("searchableFields")
    private String searchableFields;
}
