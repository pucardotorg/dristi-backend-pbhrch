package org.pucar.dristi.caselifecycle.transformer.internal.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

import java.util.HashMap;
import java.util.Map;

// Transformer @KafkaListeners cast payload.value() directly to String and run
// Jackson manually. The monolith default ConsumerFactory uses HashMapDeserializer
// (see dristi-app/src/main/resources/application.yml), so transformer needs its
// own factory wired to StringDeserializer. Every transformer @KafkaListener must
// reference this factory via containerFactory = "stringKafkaListenerContainerFactory".
@Configuration
public class TransformerKafkaConfig {

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    @Value("${kafka.consumer.config.auto_offset_reset:earliest}")
    private String autoOffsetReset;

    @Value("${kafka.consumer.config.auto_commit:true}")
    private boolean autoCommit;

    @Value("${kafka.consumer.config.auto_commit_interval:100}")
    private int autoCommitInterval;

    @Value("${kafka.consumer.config.session_timeout:15000}")
    private int sessionTimeout;

    @Bean
    public ConsumerFactory<String, Object> transformerConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "transformer");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, autoCommit);
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, autoCommitInterval);
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, sessionTimeout);
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean("stringKafkaListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, Object> stringKafkaListenerContainerFactory(
            ConsumerFactory<String, Object> transformerConsumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(transformerConsumerFactory);
        return factory;
    }
}
