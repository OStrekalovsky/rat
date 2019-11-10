package net.ostrekalovsky.rat.service;

public interface ReceiptProcessor {

    void parseAndStore() throws ReceiptsImportException;
}
