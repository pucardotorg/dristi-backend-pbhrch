// HAND-CURATED — lifted by Phase 3.5 (contract-lift)
package org.pucar.dristi.common.contract.order;


import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * <h1>IdRequest</h1>
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
public class IdRequest {

	@Size(max = 200)
	@JsonProperty("idName")
	@NotNull
	private String idName;

	@NotNull
	@Size(max=200)
	@JsonProperty("tenantId")
	private String tenantId;

	@Size(max = 200)
	@JsonProperty("format")
	private String format;

	@JsonProperty("count")
	private Integer count;

	@JsonProperty("isSequencePadded")
	private Boolean isSequencePadded = true;

}
