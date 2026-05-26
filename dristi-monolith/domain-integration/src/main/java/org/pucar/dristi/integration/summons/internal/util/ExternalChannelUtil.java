package org.pucar.dristi.integration.summons.internal.util;

import org.pucar.dristi.integration.summons.internal.channel.ChannelFactory;
import org.pucar.dristi.integration.summons.internal.channel.ExternalChannel;
import org.pucar.dristi.integration.summons.internal.web.models.*;
import org.pucar.dristi.common.contract.summons.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ExternalChannelUtil {

    private final ChannelFactory channelFactory;

    @Autowired
    public ExternalChannelUtil(ChannelFactory channelFactory) {
        this.channelFactory = channelFactory;
    }

    public ChannelMessage sendSummonsByDeliveryChannel(TaskRequest request, SummonsDelivery summonsDelivery) {
        ExternalChannel externalDeliveryChannel = channelFactory.getDeliveryChannel(summonsDelivery.getChannelName());
        return externalDeliveryChannel.sendSummons(request);
    }
}
