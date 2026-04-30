// HAND-CURATED — do not regenerate
package org.pucar.dristi.common.kafka;

import org.springframework.stereotype.Service;

/**
 * Canonical Kafka producer for dristi-common.
 *
 * <p>Delegates the actual send to {@link KafkaProducerService} so that
 * structured success/failure logging is preserved for every push and
 * errored topic data is captured. Services that need richer behaviour
 * (e.g. CTC's batch wrapper, hearing's outbox) keep their own
 * service-local helpers that autowire this Producer.
 */
@Service("commonProducer")
public class Producer {

    private final KafkaProducerService kafkaProducerService;

    public Producer(KafkaProducerService kafkaProducerService) {
        this.kafkaProducerService = kafkaProducerService;
    }

    public void push(String topic, Object value) {
        kafkaProducerService.send(topic, value);
    }
}
