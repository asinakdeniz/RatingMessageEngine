package rating.engine.billingline.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;
import rating.engine.billingline.dto.BatchResult;
import rating.engine.billingline.persistence.BillingLineEntity;
import rating.engine.billingline.persistence.BillingLineRepository;
import rating.engine.billingline.publisher.BillingLinePublisher;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import static org.springframework.util.CollectionUtils.isEmpty;
import static rating.engine.billingline.persistence.BillingLineStatus.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class BillingProcessService {

    private static final int BATCH_SIZE = 5000;

    private final BillingLineRepository billingLineRepository;
    private final BillingLinePublisher billingLinePublisher;
    private final ObjectMapper objectMapper;

    @Transactional
    public BatchResult processBillingLines(int batchNumber) {

        StopWatch batchStopWatch = new StopWatch();
        batchStopWatch.start();

        List<BillingLineEntity> unprocessedRecords = billingLineRepository.findAndLockUnprocessed(UNPROCESSED, BATCH_SIZE);

        if (isEmpty(unprocessedRecords)) {
            return null;
        }

        BatchResult batchResult = processBatchInParallel(unprocessedRecords);

        if (!batchResult.successIds().isEmpty()) {
            billingLineRepository.updateStatusByIds(PROCESSED, batchResult.successIds().toArray(Long[]::new));
        }

        if (!batchResult.failedIds().isEmpty()) {
            billingLineRepository.updateStatusByIds(FAILED, batchResult.failedIds().toArray(Long[]::new));
        }

        batchStopWatch.stop();
        log.info("Batch #{} completed in {}s: processed={}, failed={}",
                batchNumber,
                batchStopWatch.getTotalTimeSeconds(),
                batchResult.successIds().size(),
                batchResult.failedIds().size());

        return batchResult;
    }

    private BatchResult processBatchInParallel(List<BillingLineEntity> billingLineDtos) {
        var successIds = ConcurrentHashMap.<Long>newKeySet();
        var failedIds = ConcurrentHashMap.<Long>newKeySet();

        var futures = billingLineDtos.parallelStream()
                .map(dto -> processAndSend(dto, successIds, failedIds))
                .toArray(CompletableFuture[]::new);

        CompletableFuture.allOf(futures).join();
        return new BatchResult(List.copyOf(successIds), List.copyOf(failedIds));
    }

    private CompletableFuture<Void> processAndSend(BillingLineEntity dto, Set<Long> successIds, Set<Long> failedIds) {
        try {
            String json = objectMapper.writeValueAsString(dto);
            return billingLinePublisher.sendAsync(json)
                    .thenRun(() -> successIds.add(dto.getId()))
                    .exceptionally(ex -> {
                        log.warn("Failed to send billing line id={}", dto.getId(), ex);
                        failedIds.add(dto.getId());
                        return null;
                    });
        } catch (Exception e) {
            log.warn("Failed to serialize billing line id={}", dto.getId(), e);
            failedIds.add(dto.getId());
            return CompletableFuture.completedFuture(null);
        }
    }

}