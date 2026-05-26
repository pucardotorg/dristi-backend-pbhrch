package org.pucar.dristi.integration.summons.internal.channel;

import org.pucar.dristi.integration.summons.internal.web.models.ChannelMessage;
import org.pucar.dristi.integration.summons.internal.web.models.TaskRequest;

public interface ExternalChannel {

    ChannelMessage sendSummons(TaskRequest request);
}
