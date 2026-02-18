package rating.engine.billingline.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import rating.engine.billingline.dto.BatchResult;

import java.util.concurrent.atomic.LongAdder;

@Slf4j
@Service
@RequiredArgsConstructor
public class BillingLineService {

    private final BillingProcessService billingProcessService;

    @Async
    public void sendAllBillingLine() {
        StopWatch totalStopWatch = new StopWatch();
        totalStopWatch.start();

        LongAdder totalProcessed = new LongAdder();
        LongAdder totalFailed = new LongAdder();
        int batchNumber = 0;

        while (true) {
            batchNumber++;

            BatchResult batchResult = billingProcessService.processBillingLines(batchNumber);

            if (batchResult == null) {
                break;
            }

            if (!batchResult.successIds().isEmpty()) {
                totalProcessed.add(batchResult.successIds().size());
            }

            if (!batchResult.failedIds().isEmpty()) {
                totalFailed.add(batchResult.failedIds().size());
            }
        }

        totalStopWatch.stop();
        log.info("Sending billing line completed: totalBatches={}, totalProcessed={}, totalFailed={}, totalTime={}s",
                batchNumber,
                totalProcessed.sum(),
                totalFailed.sum(),
                totalStopWatch.getTotalTimeSeconds());
    }

}