package rating.engine.billingline.publisher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import rating.engine.infrastructure.kafka.KafkaConfigProperties;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

@Slf4j
@Component
@RequiredArgsConstructor
public class BillingLinePublisher {

    private final KafkaConfigProperties kafkaConfigProperties;
    private final KafkaTemplate<String, String> billingLineDataKafkaTemplate;

    public CompletableFuture<Void> sendAsync(String value) {
        return billingLineDataKafkaTemplate.send(kafkaConfigProperties.getBillingLineTopic(), value)
                .thenRun(() -> {
                })
                .exceptionally(ex -> {
                    log.error("Failed to send billing line {}", value, ex);
                    throw new CompletionException(ex);
                });
    }

}
