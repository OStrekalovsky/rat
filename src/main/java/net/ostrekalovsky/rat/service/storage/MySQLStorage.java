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
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;

@Slf4j
@Component
public class MySQLStorage implements ReceiptStorage {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbc;

    @Transactional
    private void storeReceipt(DBState state, Receipt receipt) {
        String queryState = "update States set offset=? where origin=?";
        String queryReceipt = "insert into Receipts (card_number, sale_date) values (?,?)";
        String queryProduct = "insert into Products (sale_id, code, name, price, count) values (?,?,?,?,?)";
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(queryState);
            ps.setInt(1, state.getOffset());
            ps.setString(2, state.getOrigin());
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
            DBState dbState = DBState.valueOf(jdbc, fileName.getName());
            saveReceipts(dbState, receipts);
        } catch (Exception e) {
            throw new ReceiptsStoreException("Failed to save receipts from file:" + fileName.getAbsolutePath(), e);
        }
        log.info("Receipts have been saved");
    }

    private void saveReceipts(DBState state, List<Receipt> receipts) {
        state.moveForward();
        for (; state.getOffset() < receipts.size(); state.moveForward()) {
            storeReceipt(state, receipts.get(state.getOffset()));
        }
    }
}
