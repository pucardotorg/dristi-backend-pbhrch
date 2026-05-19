package org.pucar.dristi.integration.treasury.internal.model;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TreasuryResponse {

    private boolean status;
    private String rek;
    private String data;
    private String hmac;
}