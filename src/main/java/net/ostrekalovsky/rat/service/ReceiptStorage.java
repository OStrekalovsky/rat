package net.ostrekalovsky.rat.service;

import java.io.File;
import java.util.List;

public interface ReceiptStorage {

    void store(File fileName, List<Receipt> receipts) throws ReceiptsStoreException;

    boolean wasFileProcessed(File file);
}
