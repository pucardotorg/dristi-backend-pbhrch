package org.pucar.dristi.caselifecycle.taskmanagement.internal.util;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("taskmanagementTaskManagementUtil")
public class TaskManagementUtil {

    public UUID generateUUID() {
        return UUID.randomUUID();
    }

    public Long getCurrentTimeInMilliSec() {
        return System.currentTimeMillis();
    }

}
