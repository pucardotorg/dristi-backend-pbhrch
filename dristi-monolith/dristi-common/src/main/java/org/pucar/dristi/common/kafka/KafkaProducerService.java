// HAND-CURATED — do not regenerate
package org.pucar.dristi.common.kafka;

import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.errors.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * Asynchronous Kafka send with structured success/failure logging.
 *
 * <p>Wraps {@link KafkaTemplate#send} so callers always log delivery
 * outcome (partition/offset on success; classified failure type on error).
 * Used as the single send-path for the canonical {@link Producer}.
 */
@Component
public class KafkaProducerService {

    private static final Logger log = LoggerFactory.getLogger(KafkaProducerService.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public KafkaProducerService(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void send(String topic, Object payload) {
        CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(topic, payload);

        future.handle((result, ex) -> {
            if (ex == null) {
                log.debug(
                        "Kafka push success. topic={}, partition={}, offset={}",
                        topic,
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset()
                );
            } else {
                Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                String failureType;
                if (cause instanceof TimeoutException) {
                    failureType = "KAFKA_UNAVAILABLE";
                } else if (cause instanceof SerializationException) {
                    failureType = "MESSAGE_REJECTED";
                } else {
                    failureType = "KAFKA_PUBLISH_FAILED";
                }
                log.error(
                        "Kafka push failed. failureType={}, topic={}, message={}",
                        failureType,
                        topic,
                        payload,
                        ex
                );
            }
            return null;
        });
    }
}
