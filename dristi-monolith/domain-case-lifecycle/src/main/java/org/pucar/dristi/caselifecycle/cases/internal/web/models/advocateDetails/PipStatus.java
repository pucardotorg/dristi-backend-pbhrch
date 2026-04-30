package org.pucar.dristi.caselifecycle.cases.internal.web.models.advocateDetails;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PipStatus {

    private String code;

    private String label;

    private Boolean isEnabled;
}

