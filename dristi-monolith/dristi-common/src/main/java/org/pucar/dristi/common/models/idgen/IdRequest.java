// HAND-CURATED — do not regenerate
package org.pucar.dristi.common.models.idgen;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * DRISTI extension of the standard eGov IdRequest that exposes the
 * {@code isSequencePadded} flag used by services that opt into
 * sequence-padded ID generation.
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
    @Size(max = 200)
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
