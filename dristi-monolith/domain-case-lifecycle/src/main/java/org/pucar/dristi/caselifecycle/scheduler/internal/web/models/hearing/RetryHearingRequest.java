package org.pucar.dristi.caselifecycle.scheduler.internal.web.models.hearing;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RetryHearingRequest {
    private HearingUpdateBulkRequest hearingRequest;
    private Boolean isRetryRequired;
}

