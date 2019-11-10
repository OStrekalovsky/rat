package net.ostrekalovsky.rat.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled service for Receipts import as a periodically executing task.
 * Is fault tolerant to exceptions during execution of the task.
 * <br/>
 * If the task will take more time then defined time interval before invocation, execution of the next cycle will be skipped.
 */
@Slf4j
@Component
public class ScheduledLoader {

    @Autowired
    private ReceiptProcessor receiptProcessor;

    @Scheduled(fixedRateString = "${rat.import-schedule-rate-ms}")
    public void runImportTask() {
        log.info("Starting import task");
        try {
            receiptProcessor.parseAndStore();
        } catch (ReceiptsImportException e) {
            log.error("Error during import task. Waiting for the next attempt", e);
        }
    }
}
