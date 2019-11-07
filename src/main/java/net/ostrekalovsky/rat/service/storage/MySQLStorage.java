package net.ostrekalovsky.rat.service.storage;

import lombok.extern.slf4j.Slf4j;
import net.ostrekalovsky.rat.service.Product;
import net.ostrekalovsky.rat.service.Receipt;
import net.ostrekalovsky.rat.service.ReceiptStorage;
import net.ostrekalovsky.rat.service.ReceiptsStoreException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.io.File;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Component
public class MySQLStorage implements ReceiptStorage {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbc;

    @Transactional
    private void storeReceipt(String origin, Receipt receipt) {
        String queryState = "replace into States(card_number, sale_date) values (?,?) where origin=?";
        String queryReceipt = "insert into Receipts (card_number, sale_date) values (?,?)";
        String queryProduct = "insert into Products (sale_id, code, name, price, count) values (?,?,?,?,?)";
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(queryState);
            ps.setString(1, receipt.getCardNumber());
            ps.setTimestamp(2, new Timestamp(receipt.getDate()));
            ps.setString(3, origin);
            return ps;
        });
        log.debug("State saved");
        GeneratedKeyHolder generatedKeyHolder = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(queryReceipt, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, receipt.getCardNumber());
            ps.setTimestamp(2, new Timestamp(receipt.getDate()));
            return ps;
        }, generatedKeyHolder);
        log.debug("Receipt saved");
        for (Product product : receipt.getProducts()) {
            jdbc.update(con -> {
                PreparedStatement ps = con.prepareStatement(queryProduct);
                ps.setInt(1, Objects.requireNonNull(generatedKeyHolder.getKey()).intValue());
                ps.setLong(2, product.getCode());
                ps.setString(3, product.getName());
                ps.setBigDecimal(4, product.getPrice());
                ps.setLong(5, product.getCount());
                return ps;
            });
        }
        log.debug("All products saved");
    }

    @Override
    public void store(File fileName, List<Receipt> receipts) throws ReceiptsStoreException {
        try {
            log.info("Saving receipts into DB from file:{}", fileName.getAbsolutePath());
            Optional<DBState> dbState = DBState.valueOf(jdbc, fileName.getName());
            if (dbState.isPresent()) {
                continueReceiptsSavingFromOriginCheckpoint(fileName.getName(), receipts, dbState.get());
            } else {
                saveReceiptsFromUnknownOrigin(fileName.getName(), receipts);
            }
        } catch (Exception e) {
            throw new ReceiptsStoreException("Failed to save receipts from file:" + fileName.getAbsolutePath(), e);
        }
        log.info("Receipts have been saved");
    }

    private void continueReceiptsSavingFromOriginCheckpoint(String originId, List<Receipt> receipts, DBState state) {
        int savedReceiptIdx = 0;
        for (int i = 0; i < receipts.size(); i++) {
            Receipt receipt = receipts.get(i);
            if (receipt.getCardNumber().equals(state.getLastSavedCardNumber()) && receipt.getDate() == state.getLastSavedSaleDate()) {
                savedReceiptIdx = i + 1;
                break;
            }
        }
        for (; savedReceiptIdx < receipts.size(); savedReceiptIdx++) {
            storeReceipt(originId, receipts.get(savedReceiptIdx));
        }
    }

    private void saveReceiptsFromUnknownOrigin(String originId, List<Receipt> receipts) {
        for (Receipt receipt : receipts) {
            storeReceipt(originId, receipt);
        }
    }
}
