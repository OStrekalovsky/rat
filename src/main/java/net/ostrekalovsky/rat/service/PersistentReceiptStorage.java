package net.ostrekalovsky.rat.service;

import net.ostrekalovsky.rat.service.storage.DBState;

public interface PersistentReceiptStorage {

    void storeReceipt(DBState state, Receipt receipt);

    DBState prepareStateForOrigin(String origin);
}
