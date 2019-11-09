package net.ostrekalovsky.rat.service.storage;

import lombok.extern.slf4j.Slf4j;
import net.ostrekalovsky.rat.service.Receipt;
import net.ostrekalovsky.rat.service.ReceiptStorage;
import net.ostrekalovsky.rat.service.ReceiptsStoreException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.File;
import java.util.List;

@Slf4j
@Service
public class Storage implements ReceiptStorage {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private MySQLDAOService dao;

    public void store(File fileName, List<Receipt> receipts) throws ReceiptsStoreException {
        try {
            log.info("Saving receipts into DB from file:{}", fileName.getAbsolutePath());
            DBState dbState = getState(fileName.getName());
            saveReceipts(dbState, receipts);
        } catch (Exception e) {
            throw new ReceiptsStoreException("Failed to save receipts from file:" + fileName.getAbsolutePath(), e);
        }
        log.info("Receipts have been saved");
    }

    private void saveReceipts(DBState state, List<Receipt> receipts) {
        state.moveForward();
        for (; state.getOffset() < receipts.size(); state.moveForward()) {
            dao.storeReceipt(state, receipts.get(state.getOffset()));
        }
    }

    private DBState getState(String origin) {
        return DBState.valueOf(jdbc, origin);
    }
}
