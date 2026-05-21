package org.pucar.dristi.caselifecycle.digitalizeddocuments.internal.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("digitalizeddocumentsDigitalizedDocumentUtil")
@Slf4j
public class DigitalizedDocumentUtil {

    public Long getCurrentTimeInMilliSec() {
        return System.currentTimeMillis();
    }

    public UUID generateUUID() {
        return UUID.randomUUID();
    }

}
