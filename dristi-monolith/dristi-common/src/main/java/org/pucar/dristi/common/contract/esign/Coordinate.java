// HAND-CURATED — lifted by Phase 35 (contract-lift)
package org.pucar.dristi.common.contract.esign;

import lombok.*;

@Builder
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Coordinate {


    private float x;
    private float y;
    private boolean found;  // Indicates if the coordinate was successfully located
    private int pageNumber; // The page number associated with this coordinate
    private String fileStoreId;
    private String tenantId;


}
