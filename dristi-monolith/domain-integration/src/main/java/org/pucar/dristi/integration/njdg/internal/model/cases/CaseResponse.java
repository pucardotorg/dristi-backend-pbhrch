package org.pucar.dristi.integration.njdg.internal.model.cases;

import org.pucar.dristi.integration.njdg.internal.model.NJDGTransformRecord;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CaseResponse {
    private ResponseInfo responseInfo;
    private List<NJDGTransformRecord> cases;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResponseInfo {
        private String status;
        private String message;
    }
}
