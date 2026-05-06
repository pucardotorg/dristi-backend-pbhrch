// HAND-CURATED — lifted by Phase 35 (contract-lift)
package org.pucar.dristi.common.contract.notification;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import lombok.*;
import org.egov.common.contract.request.RequestInfo;

import java.util.List;


/**
 * <h1>IdGenerationRequest</h1>
 * 
 * @author Narendra
 *
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class IdGenerationRequest {

	@JsonProperty("RequestInfo")
	private RequestInfo requestInfo;

	@Valid
	private List<IdRequest> idRequests;

}
