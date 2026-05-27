package org.pucar.dristi.common.contract.hearingmanagement;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum SortOrder {
    ASC("asc"), DESC("desc");

    private final String value;

    SortOrder(String value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
        return String.valueOf(value);
    }

    @JsonCreator
    public static SortOrder fromValue(String text) {
        for (SortOrder b : SortOrder.values()) {
            if (String.valueOf(b.value).equals(text)) {
                return b;
            }
        }
        return null;
    }
}
