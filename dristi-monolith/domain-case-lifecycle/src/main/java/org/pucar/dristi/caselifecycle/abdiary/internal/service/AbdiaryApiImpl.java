package org.pucar.dristi.caselifecycle.abdiary.internal.service;

import lombok.RequiredArgsConstructor;
import org.pucar.dristi.caselifecycle.abdiary.AbdiaryApi;
import org.pucar.dristi.common.contract.abdiary.BulkDiaryEntryRequest;
import org.pucar.dristi.common.contract.abdiary.CaseDiaryEntry;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("abdiaryApiImpl")
@RequiredArgsConstructor
public class AbdiaryApiImpl implements AbdiaryApi {

    private final DiaryEntryService diaryEntryService;

    @Override
    public List<CaseDiaryEntry> createBulkDiaryEntries(BulkDiaryEntryRequest request) {
        return diaryEntryService.bulkDiaryEntry(request);
    }
}
