package org.pucar.dristi.integration.summons.internal.channel;

import org.pucar.dristi.common.contract.summons.ChannelMessage;
import org.pucar.dristi.common.contract.summons.TaskRequest;

public interface ExternalChannel {

    ChannelMessage sendSummons(TaskRequest request);
}
