// HAND-CURATED — lifted by Phase 35 (contract-lift)
package org.pucar.dristi.common.contract.esign;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@Builder
public class StorageResponse {

    @JsonProperty("files")
    private List<File> files;

    @JsonCreator
    public StorageResponse(@JsonProperty("files") List<File> files) {
        this.files = files;
    }
}
