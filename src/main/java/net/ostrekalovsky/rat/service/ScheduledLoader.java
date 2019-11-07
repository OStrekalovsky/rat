package net.ostrekalovsky.rat.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ScheduledLoader {

    @Autowired
    private ReceiptParser receiptParser;

    @Scheduled(fixedRateString = "${rat.import-schedule-rate-ms}")
    public void runImportTask() {
        log.info("Starting import task");
        try {
            receiptParser.parseAndStore();
        } catch (ReceiptsImportException e) {
            log.error("Error during import task. Waiting for the next attempt", e);
        }
    }
}
