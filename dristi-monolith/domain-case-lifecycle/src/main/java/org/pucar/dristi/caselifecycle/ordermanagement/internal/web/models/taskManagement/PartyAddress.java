package org.pucar.dristi.caselifecycle.ordermanagement.internal.web.models.taskManagement;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.pucar.dristi.caselifecycle.ordermanagement.internal.web.models.courtCase.AddressDetails;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PartyAddress {
    @JsonProperty("addressDetails")
    private AddressDetails addressDetails;

    @JsonProperty("id")
    private String id;
}
