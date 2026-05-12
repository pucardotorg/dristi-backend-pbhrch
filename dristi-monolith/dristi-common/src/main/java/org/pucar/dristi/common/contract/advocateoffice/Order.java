// HAND-CURATED — lifted by Phase 35 enum promotion
package org.pucar.dristi.common.contract.advocateoffice;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Order {

    ASC("asc"), DESC("desc");

    private final String value;

    Order(String value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
        return String.valueOf(value);
    }

    @JsonCreator
    public static Order fromValue(String text) {
        for (Order b : Order.values()) {
            if (String.valueOf(b.value).equals(text)) {
                return b;
            }
        }
        return null;
    }
}
