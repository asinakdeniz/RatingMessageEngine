package rating.engine.billingline.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import rating.engine.billingline.persistence.BillingLineEntity;
import rating.engine.billingline.persistence.BillingLineRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static rating.engine.billingline.persistence.BillingLineStatus.UNPROCESSED;

@Slf4j
@Service
@RequiredArgsConstructor
public class BillingLineTestDataGenerator {

    private final BillingLineRepository billingLineRepository;

    @Async
    public void generateBillingLine() {
        int batchSize = 1000;
        int totalBatches = 500;
        for (long i = 0; i < totalBatches; i++) {
            List<BillingLineEntity> billingLineEntities = new ArrayList<>();
            for (long j = 0; j < batchSize; j++) {
                Long uniqueId = i * batchSize + j;
                BillingLineEntity entity = createBillingLineEntity(uniqueId);
                billingLineEntities.add(entity);
            }
            billingLineRepository.saveAll(billingLineEntities);
        }
    }

    private BillingLineEntity createBillingLineEntity(Long id) {
        return BillingLineEntity.builder()
                .contractId("CONTRACT_" + id)
                .startDate(Instant.now())
                .endDate(Instant.now())
                .productId("PRODUCT_1")
                .consumption(BigDecimal.valueOf(12))
                .status(UNPROCESSED)
                .build();
    }


}
