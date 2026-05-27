package org.pucar.dristi.caselifecycle.hearingmanagement.internal.config;

import lombok.Getter;

@Getter
public enum HearingSlotStatus {

    COURT_NON_WORKING("Court Non-Working");

    private final String value;

    HearingSlotStatus(String value) {
        this.value = value;
    }
}
