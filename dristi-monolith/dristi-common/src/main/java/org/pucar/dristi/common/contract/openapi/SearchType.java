// HAND-CURATED — lifted by Phase 35 (contract-lift)
package org.pucar.dristi.common.contract.openapi;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum SearchType {
    FILING_NUMBER("filing_number"),
    CASE_NUMBER("case_number"),
    CNR_NUMBER("cnr_number"),
    ADVOCATE("advocate"),
    LITIGANT("litigant"),
    ALL("all");

    private final String value;

    SearchType(String value) {
        this.value = value;
    }

    @JsonCreator
    public static SearchType fromValue(String text) {
        for (SearchType type : SearchType.values()) {
            if (type.value.equalsIgnoreCase(text)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown SearchType: " + text);
    }

    @JsonValue
    public String toValue() {
        return value;
    }
}
