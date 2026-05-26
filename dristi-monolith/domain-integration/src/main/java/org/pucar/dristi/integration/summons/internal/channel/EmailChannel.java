package org.pucar.dristi.integration.summons.internal.channel;

import org.pucar.dristi.common.kafka.Producer;
import org.pucar.dristi.integration.summons.internal.web.models.ChannelMessage;
import org.pucar.dristi.integration.summons.internal.web.models.TaskRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class EmailChannel implements ExternalChannel{

    private final Producer producer;


    public EmailChannel(Producer producer) {
        this.producer = producer;
    }

    @Override
    public ChannelMessage sendSummons(TaskRequest request) {
        producer.push("egov.core.notification.email", request);
        return ChannelMessage.builder().acknowledgementStatus("success").build();
    }
}
