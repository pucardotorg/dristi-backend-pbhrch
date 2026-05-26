package org.pucar.dristi.caselifecycle.casemanagement.internal.service;

import org.pucar.dristi.caselifecycle.casemanagement.internal.web.models.BundleData;
import org.pucar.dristi.caselifecycle.casemanagement.internal.web.models.CaseBundleNode;
import org.pucar.dristi.common.contract.casemanagement.CourtCase;
import org.pucar.dristi.caselifecycle.casemanagement.internal.web.models.docpreview.DocPreviewRequest;

public interface CaseBundleSection {

    String getOrder();

    default String getSectionKey() {
        return getClass().getSimpleName();
    }

    CaseBundleNode build(BundleData data);
}
