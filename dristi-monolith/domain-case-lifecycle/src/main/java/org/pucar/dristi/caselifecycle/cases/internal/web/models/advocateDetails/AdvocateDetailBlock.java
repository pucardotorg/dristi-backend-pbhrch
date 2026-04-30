package org.pucar.dristi.caselifecycle.cases.internal.web.models.advocateDetails;

import lombok.*;
import org.pucar.dristi.caselifecycle.cases.internal.web.models.Advocate;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdvocateDetailBlock {

    private Complainant complainant;

    private PipStatus isComplainantPip;

    private UiFlags uiFlags;

    private Documents documents;

    private List<Advocate> advocates;

    private Integer advocateCount;

    private Integer displayIndex;

    private Boolean isEnabled;

    private Boolean isFormCompleted;
}
