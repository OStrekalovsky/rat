package net.ostrekalovsky.rat.service.storage;

import io.micrometer.core.annotation.Timed;
import lombok.extern.slf4j.Slf4j;
import net.ostrekalovsky.rat.service.Receipt;
import net.ostrekalovsky.rat.service.ReceiptStorage;
import net.ostrekalovsky.rat.service.ReceiptsStoreException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;


/**
 * Receipts Storage implementation which can handle interruptions during uploads and prevents data duplication in DAO.
 * Due to absence of any kind 'natural' or surrogate idempotency key in presented data set to prevent duplicated inserts
 * the only options will be to suppose that no 'real' receipt will be mentioned several times among files or in one of the files.
 * <br/>
 * So to prevent multiple insertions of receipts from single file creates idempotency key as pair of file name and entity offset in this file.
 * This idempotency key will be transactionally recorded along with the receipt entity with which it is associated.
 */
@Slf4j
@Service
public class RenewableStorageService implements ReceiptStorage {

    @Autowired
    private MySQLDAOService dao;

    @Timed(description = "Time spent saving file to db", value = "rat.file-save", longTask = true)
    public void store(File fileName, List<Receipt> receipts) throws ReceiptsStoreException {
        try {
            log.info("Saving receipts into DB from file:{}", fileName.getAbsolutePath());
            DBState dbState = dao.prepareStateForOrigin(fileName.getName());
            saveReceipts(dbState, receipts);
        } catch (Exception e) {
            throw new ReceiptsStoreException("Failed to save receipts from file:" + fileName.getAbsolutePath(), e);
        }
        log.info("Receipts have been saved");
    }

    @Override
    public boolean wasFileProcessed(File file) {
        return dao.prepareStateForOrigin(file.getName()).isProcessed();
    }


    private void saveReceipts(DBState state, List<Receipt> receipts) {
        state.moveForward();
        for (; state.getOffset() < receipts.size(); state.moveForward()) {
            if (state.getOffset() == receipts.size() - 1) {
                state.setProcessed();
            }
            dao.storeReceipt(state, receipts.get(state.getOffset()));
        }
    }
}
