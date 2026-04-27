// AUTO-EXTRACTED INTO dristi-common BY scripts/migration/dristi_common/03_build_canonical.py
// Source: dristi-services/hearing/src/main/java/org/pucar/dristi/kafka/Producer.java
// NOTE: imports referencing service-internal classes (ServiceConstants,
// Configuration, web.models.*) may need follow-up — see Phase 4.
package org.pucar.dristi.common.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class Producer {

    @Autowired
    private final KafkaProducerService kafkaProducerService;

    public Producer(KafkaProducerService kafkaProducerService) {
        this.kafkaProducerService = kafkaProducerService;
    }

    public void push(String topic, Object value) {
        kafkaProducerService.send(topic, value);
    }
}
