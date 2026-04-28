package org.pucar.dristi.caselifecycle.cases.internal.web.models.advocateDetails;

import lombok.*;
import org.pucar.dristi.caselifecycle.cases.internal.web.models.Document;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Documents {

    private List<Document> vakalatnama;

    private List<Document> pipAffidavit;
}

