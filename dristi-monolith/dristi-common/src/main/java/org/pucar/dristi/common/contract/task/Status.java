// HAND-CURATED — lifted by C2 to back contract DTOs (TaskExists, CollectedReceipt) that
// were Phase-35-moved to dristi-common but kept stale imports into task internals.
package org.pucar.dristi.common.contract.task;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Status {

    CREATED("CREATED"),

    CANCELLED("CANCELLED"),

    INSTRUMENT_BOUNCED("INSTRUMENT_BOUNCED");

    private String value;

    Status(String value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
        return String.valueOf(value);
    }

    @JsonCreator
    public static Status fromValue(String text) {
        for (Status b : Status.values()) {
            if (String.valueOf(b.value).equalsIgnoreCase(text)) {
                return b;
            }
        }
        return null;
    }
}
