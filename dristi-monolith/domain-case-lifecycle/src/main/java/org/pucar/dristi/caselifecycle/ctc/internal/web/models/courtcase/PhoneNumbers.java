package org.pucar.dristi.caselifecycle.ctc.internal.web.models.courtcase;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class PhoneNumbers {
    private List<String> mobileNumber = new ArrayList<>();
    private String textfieldValue;
}
