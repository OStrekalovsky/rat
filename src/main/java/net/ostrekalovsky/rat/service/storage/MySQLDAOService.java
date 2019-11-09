package net.ostrekalovsky.rat.service.storage;

import lombok.extern.slf4j.Slf4j;
import net.ostrekalovsky.rat.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
public class MySQLDAOService implements ReportService {

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

    @Override
    public Optional<FavouriteReport> getFavouriteProductsByCard(String card, int limit) {
        log.info("Request: getFavouriteProductsByCard, card={}, limit={}", card, limit);
        //TODO: explain needed
        String sql = "select distinct Products.name as name, Products.code as code, T.count as count from (select sum(Products.count) as count, code from Products INNER JOIN Receipts ON Products.sale_id=Receipts.sale_id WHERE Receipts.card_number=? GROUP BY Products.code ORDER BY count desc limit ?) as T INNER JOIN Products ON Products.code=T.code ORDER BY count desc";
        List<ProductProjection> results = jdbc.query(sql,
                new Object[]{card, limit},
                (rs, rowNum) -> new ProductProjection(rs.getString("name"),
                        rs.getInt("count"),
                        rs.getInt("code")));

        if (results.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(new FavouriteReport(results));
        }
    }

    @Override
    public DailyReport getDailyReport(LocalDate date) {
        log.info("Request: getDailyReport, date={}", date);
        LocalDate nextDay = date.plusDays(1);
        String sql = "select sum(Products.price * Products.count) AS totalSum from Products INNER JOIN Receipts ON Products.sale_id=Receipts.sale_id WHERE Receipts.sale_date>=? and Receipts.sale_date<?";
        List<BigDecimal> results = jdbc.query(sql,
                new Object[]{date.format(DateTimeFormatter.ISO_DATE), nextDay.format(DateTimeFormatter.ISO_DATE)},
                (rs, rowNum) -> rs.getBigDecimal("totalSum"));
        if (results.size() != 1) {
            throw new RuntimeException("Aggregated request returns more then one row:" + results);
        }
        BigDecimal result = results.get(0);
        if (Objects.isNull(result)) {
            return new DailyReport(date, new BigDecimal(0));
        } else {
            return new DailyReport(date, result);
        }
    }
}
