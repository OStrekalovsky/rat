package net.ostrekalovsky.rat.service.storage;

import lombok.extern.slf4j.Slf4j;
import net.ostrekalovsky.rat.service.Product;
import net.ostrekalovsky.rat.service.Receipt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Objects;

@Slf4j
@Service
public class MySQLDAOService {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbc;

    @Transactional
    public void storeReceipt(DBState state, Receipt receipt) {
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
        jdbc.batchUpdate(queryProduct, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Product product = receipt.getProducts().get(i);
                ps.setInt(1, Objects.requireNonNull(generatedKeyHolder.getKey()).intValue());
                ps.setLong(2, product.getCode());
                ps.setString(3, product.getName());
                ps.setBigDecimal(4, product.getPrice());
                ps.setLong(5, product.getCount());
            }

            @Override
            public int getBatchSize() {
                return receipt.getProducts().size();
            }
        });
        log.debug("All products saved");
        log.info("Receipt saved. State={}", state);
    }
}
