package net.ostrekalovsky.rat.service;

import net.ostrekalovsky.rat.service.storage.DBState;

public interface PersistentStorage {

    void storeReceipt(DBState state, Receipt receipt);

    DBState prepareStateForOrigin(String origin);
}
