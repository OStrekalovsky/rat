package net.ostrekalovsky.rat.service.storage;

import lombok.extern.slf4j.Slf4j;
import net.ostrekalovsky.rat.service.Receipt;
import net.ostrekalovsky.rat.service.ReceiptStorage;
import net.ostrekalovsky.rat.service.ReceiptsStoreException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.io.File;
import java.util.List;

@Slf4j
@Service
public class ResumableStorage implements ReceiptStorage {

    @Autowired
    private MySQLDAOService dao;


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
            if (state.getOffset()==receipts.size()-1){
                state.setProcessed();
            }
            dao.storeReceipt(state, receipts.get(state.getOffset()));
        }
    }
}
