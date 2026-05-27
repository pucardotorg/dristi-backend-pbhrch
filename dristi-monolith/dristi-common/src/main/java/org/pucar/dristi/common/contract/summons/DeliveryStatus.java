// HAND-CURATED — lifted by Phase 35 (contract-lift)
package org.pucar.dristi.common.contract.summons;

public enum DeliveryStatus {

    NOT_DELIVERED,
    NOT_DELIVERED_ICOPS,

    IN_TRANSIT,

    DELIVERED,
    DELIVERED_ICOPS,

    NOT_UPDATED,

    STATUS_UNKNOWN,
    EXECUTED,

    NOT_EXECUTED,

    // epost intermediate status
    INTERMEDIATE
}
