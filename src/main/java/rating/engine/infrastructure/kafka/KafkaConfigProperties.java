package rating.engine.infrastructure.kafka;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.kafka.common.security.auth.SecurityProtocol.SASL_PLAINTEXT;
import static org.apache.kafka.common.security.auth.SecurityProtocol.SASL_SSL;

@Data
@Validated
@ConfigurationProperties("spring.kafka")
public class KafkaConfigProperties {

    @NotEmpty
    private final String bootstrapServers;

    @NotEmpty
    private final String clientId;

    @NotNull
    private final URI schemaRegistryUrl;

    @NotEmpty
    private final String billingLineTopic;

    private final boolean autoRegisterSchemas;
    private final Sasl sasl;
    private final Security security;

    /**
     * For additional configuration if properties like
     * delivery.timeout.ms,
     * batch.size
     * need to be changed.
     * Consider creating fields.
     */
    private final Map<@NotEmpty String, @NotEmpty String> producer = new HashMap<>();

    @Getter
    @Validated
    @RequiredArgsConstructor
    public static class Sasl {

        private final String mechanism;

        @NotEmpty
        private final String jaasConfig;

    }

    @Getter
    @Validated
    @RequiredArgsConstructor
    public static class Security {

        @NotEmpty
        private final String protocol;

    }

    public Map<String, Object> buildCommonProperties() {
        Map<String, Object> properties = new HashMap<>();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.put(ProducerConfig.CLIENT_ID_CONFIG, clientId);
        properties.put(ProducerConfig.ACKS_CONFIG, "all");
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        properties.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, security.getProtocol());

        if (List.of(SASL_PLAINTEXT.name(), SASL_SSL.name()).contains(security.getProtocol())) {
            properties.put(SaslConfigs.SASL_MECHANISM, sasl.getMechanism());
            properties.put(SaslConfigs.SASL_JAAS_CONFIG, sasl.getJaasConfig());
        }
        return properties;
    }

}
