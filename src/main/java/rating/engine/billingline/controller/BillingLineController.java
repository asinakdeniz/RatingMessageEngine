package rating.engine.billingline.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import rating.engine.billingline.service.BillingLineService;
import rating.engine.billingline.service.BillingLineTestDataGenerator;

import static org.springframework.http.HttpStatus.ACCEPTED;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/billing-lines")
public class BillingLineController {

    private final BillingLineService billingLineService;
    private final BillingLineTestDataGenerator billingLineTestDataGenerator;

    @PostMapping("/process")
    @ResponseStatus(ACCEPTED)
    public void processBillingLines() {
        log.info("Starting billing line processing");
        billingLineService.sendAllBillingLine();
    }

    @PostMapping("/test-data")
    @ResponseStatus(ACCEPTED)
    public void generateTestData() {
        log.info("Starting test data generation");
        billingLineTestDataGenerator.generateBillingLine();
    }

}