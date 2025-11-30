package rating.engine.infrastructure.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.Map;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(KafkaConfigProperties.class)
public class KafkaProducerConfiguration {

    private final KafkaProperties kafkaProperties;
    private final KafkaConfigProperties kafkaConfigProperties;

    @Bean
    public KafkaTemplate<String, String> billingLineDataKafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    @Bean
    public <T> ProducerFactory<String, T> producerFactory() {
        return new DefaultKafkaProducerFactory<>(producerDefaultProperties());
    }

    private Map<String, Object> producerDefaultProperties() {
        Map<String, Object> properties = kafkaProperties.buildProducerProperties();
        properties.putAll(kafkaConfigProperties.buildCommonProperties());
        return properties;
    }

}
