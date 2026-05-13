package org.pucar.dristi.caselifecycle.task.internal.enrichment;

import org.pucar.dristi.caselifecycle.task.internal.web.models.TaskRequest;

public interface TopicStrategy {

    boolean canPush(String status);
    void pushToTopic(TaskRequest taskRequest);

}

