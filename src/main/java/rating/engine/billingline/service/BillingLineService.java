package rating.engine.billingline.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import rating.engine.billingline.persistence.BillingLineEntity;
import rating.engine.billingline.persistence.BillingLineRepository;
import rating.engine.billingline.publisher.BillingLinePublisher;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

import static org.springframework.util.CollectionUtils.isEmpty;
import static rating.engine.billingline.persistence.BillingLineStatus.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class BillingLineService {

    private static final int BATCH_SIZE = 1000;

    private final BillingLineRepository billingLineRepository;
    private final BillingLinePublisher billingLinePublisher;
    private final ObjectMapper objectMapper;

    @Async
    public void sendAllBillingLine() {
        StopWatch totalStopWatch = new StopWatch();
        totalStopWatch.start();

        LongAdder totalProcessed = new LongAdder();
        LongAdder totalFailed = new LongAdder();
        int batchNumber = 0;

        while (true) {
            batchNumber++;
            StopWatch batchStopWatch = new StopWatch();
            batchStopWatch.start();

            List<BillingLineEntity> unprocessedRecords =
                    billingLineRepository.findByStatusWithLimit(UNPROCESSED, BATCH_SIZE);

            if (isEmpty(unprocessedRecords)) {
                break;
            }

            var result = processBatchInParallel(unprocessedRecords);

            if (!result.successIds().isEmpty()) {
                billingLineRepository.updateStatusByIds(PROCESSED, result.successIds());
                totalProcessed.add(result.successIds().size());
            }

            if (!result.failedIds().isEmpty()) {
                billingLineRepository.updateStatusByIds(FAILED, result.failedIds());
                totalFailed.add(result.failedIds().size());
            }

            batchStopWatch.stop();
            log.info("Batch #{} completed in {}s: processed={}, failed={}",
                    batchNumber,
                    batchStopWatch.getTotalTimeSeconds(),
                    result.successIds().size(),
                    result.failedIds().size());
        }

        totalStopWatch.stop();
        log.info("Sending billing line completed: totalBatches={}, totalProcessed={}, totalFailed={}, totalTime={}s",
                batchNumber,
                totalProcessed.sum(),
                totalFailed.sum(),
                totalStopWatch.getTotalTimeSeconds());
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

    private record BatchResult(List<Long> successIds, List<Long> failedIds) {
    }

}