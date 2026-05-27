package org.pucar.dristi.caselifecycle.ordermanagement.internal.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.pucar.dristi.caselifecycle.abdiary.AbdiaryApi;
import org.pucar.dristi.caselifecycle.ordermanagement.internal.web.models.adiary.BulkDiaryEntryRequest;
import org.pucar.dristi.caselifecycle.ordermanagement.internal.web.models.adiary.BulkDiaryEntryResponse;
import org.pucar.dristi.caselifecycle.ordermanagement.internal.web.models.adiary.CaseDiaryEntry;

import java.util.Collections;
import java.util.List;

import static org.pucar.dristi.caselifecycle.ordermanagement.internal.config.ServiceConstants.SEARCHER_SERVICE_EXCEPTION;

@Component("ordermanagementADiaryUtil")
@Slf4j
public class ADiaryUtil {

    private final ObjectMapper objectMapper;
    private final AbdiaryApi abdiaryApi;

    @Autowired
    public ADiaryUtil(ObjectMapper objectMapper, AbdiaryApi abdiaryApi) {
        this.objectMapper = objectMapper;
        this.abdiaryApi = abdiaryApi;
    }

    public BulkDiaryEntryResponse createBulkADiaryEntry(BulkDiaryEntryRequest request) {
        try {
            org.pucar.dristi.common.contract.abdiary.BulkDiaryEntryRequest bridgedRequest =
                    objectMapper.convertValue(request,
                            org.pucar.dristi.common.contract.abdiary.BulkDiaryEntryRequest.class);
            List<org.pucar.dristi.common.contract.abdiary.CaseDiaryEntry> apiResult =
                    abdiaryApi.createBulkDiaryEntries(bridgedRequest);
            List<CaseDiaryEntry> bridgedResult = apiResult == null ? Collections.emptyList() :
                    apiResult.stream()
                            .map(entry -> objectMapper.convertValue(entry, CaseDiaryEntry.class))
                            .toList();
            return BulkDiaryEntryResponse.builder()
                    .caseDiaryEntries(bridgedResult)
                    .build();
        } catch (Exception e) {
            log.error(SEARCHER_SERVICE_EXCEPTION, e);
            throw new CustomException();
        }
    }
}
