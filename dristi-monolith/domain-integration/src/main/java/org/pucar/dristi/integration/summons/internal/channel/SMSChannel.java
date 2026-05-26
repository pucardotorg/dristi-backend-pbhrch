package org.pucar.dristi.integration.summons.internal.channel;

import org.pucar.dristi.integration.summons.internal.config.Configuration;
import org.pucar.dristi.common.kafka.Producer;
import org.pucar.dristi.integration.summons.internal.web.models.ChannelMessage;
import org.pucar.dristi.integration.summons.internal.web.models.ChannelResponse;
import org.pucar.dristi.integration.summons.internal.web.models.TaskRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
public class SMSChannel implements ExternalChannel{


    private final Producer producer;


    public SMSChannel(Producer producer) {
        this.producer = producer;
    }

    @Override
    public ChannelMessage sendSummons(TaskRequest request) {
        producer.push("egov.core.notification.sms", request);
        return ChannelMessage.builder().acknowledgementStatus("success").build();
    }
}
