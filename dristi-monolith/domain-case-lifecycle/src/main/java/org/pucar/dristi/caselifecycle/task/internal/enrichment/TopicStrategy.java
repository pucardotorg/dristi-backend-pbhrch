package org.pucar.dristi.caselifecycle.task.internal.enrichment;

import org.pucar.dristi.common.contract.task.TaskRequest;

public interface TopicStrategy {

    boolean canPush(String status);
    void pushToTopic(TaskRequest taskRequest);

}

