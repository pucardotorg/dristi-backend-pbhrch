package org.pucar.dristi.caselifecycle.abdiary;

import org.pucar.dristi.common.contract.abdiary.BulkDiaryEntryRequest;
import org.pucar.dristi.common.contract.abdiary.CaseDiaryEntry;

import java.util.List;

/**
 * Public, cross-subdomain API of the ab-diary subdomain. Other modules
 * (order-management today) consume ab-diary through this interface —
 * never by importing from {@code internal/}.
 *
 * <p>Contract DTOs live at {@code dristi-common/contract/abdiary/}
 * (Phase-35-lifted during the ab-diary migration).
 */
public interface AbdiaryApi {

    /**
     * Add a bulk batch of case-diary entries — mirrors the
     * {@code /ab-diary/case/diary/v1/bulkEntry} REST endpoint. Returns
     * the per-entry persisted result list.
     */
    List<CaseDiaryEntry> createBulkDiaryEntries(BulkDiaryEntryRequest request);
}
