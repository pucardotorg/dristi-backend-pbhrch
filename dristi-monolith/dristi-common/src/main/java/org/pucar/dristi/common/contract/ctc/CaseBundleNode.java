// HAND-CURATED — lifted by Phase 35 (contract-lift)
package org.pucar.dristi.common.contract.ctc;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CaseBundleNode {

    private String id;
    private String title;
    private String fileStoreId;
    private String issuedFileStoreId;
    private String status;//accepted rejected pending
    private List<CaseBundleNode> children;
}
