// HAND-CURATED — lifted by Phase 35 (contract-lift)
package org.pucar.dristi.common.contract.scheduler;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RecentCauseList {
    @JsonProperty("courtId")
    private String courtId;

    @JsonProperty("fileStoreId")
    private String fileStoreId;

    @JsonProperty("date")
    private LocalDate date = null;

}
