package org.pucar.dristi.caselifecycle.abdiary.internal.util;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("abdiaryADiaryUtil")
public class ADiaryUtil {

    public Long getCurrentTimeInMilliSec() {
        return System.currentTimeMillis();
    }

    public UUID generateUUID() {
        return UUID.randomUUID();
    }

}
