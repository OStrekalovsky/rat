package net.ostrekalovsky.rat.service;

public interface ReceiptParser {

    void parseAndStore() throws ReceiptsImportException;
}
