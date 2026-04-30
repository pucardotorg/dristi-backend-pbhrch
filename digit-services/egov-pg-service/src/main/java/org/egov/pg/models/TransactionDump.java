package org.egov.pg.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransactionDump {

    @JsonProperty("txnId")
    private String txnId;

    @JsonProperty("txnRequest")
    private String txnRequest;

    @JsonProperty("txnResponse")
    private Object txnResponse;

    @JsonProperty("auditDetails")
    private AuditDetails auditDetails;


}
