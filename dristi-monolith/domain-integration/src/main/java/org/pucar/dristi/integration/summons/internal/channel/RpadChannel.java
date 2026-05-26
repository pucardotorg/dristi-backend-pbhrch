package org.pucar.dristi.integration.summons.internal.channel;

import org.pucar.dristi.common.contract.summons.ChannelMessage;
import org.pucar.dristi.common.contract.summons.TaskRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RpadChannel implements ExternalChannel {


    @Override
    public ChannelMessage sendSummons(TaskRequest request) {
        log.info("Rpad channel is used for task: {}", request.getTask().getTaskNumber());
        return ChannelMessage.builder().acknowledgementStatus("success").build();
    }
}
