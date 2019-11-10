package net.ostrekalovsky.rat.service.storage;

import lombok.extern.slf4j.Slf4j;
import net.ostrekalovsky.rat.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
public class MySQLDAOService implements ReportService, PersistentStorage {

    @Autowired
    private JdbcTemplate jdbc;

    @Override
    @Transactional
    public void storeReceipt(DBState state, Receipt receipt) {
        String queryState = "update States set offset=?, processed=? where origin=?";
        String queryReceipt = "insert into Receipts (card_number, sale_date, sum) values (?,?,?)";
        String queryProduct = "insert into Products (sale_id, code, name, price, count) values (?,?,?,?,?)";
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(queryState);
            ps.setInt(1, state.getOffset());
            ps.setBoolean(2, state.isProcessed());
            ps.setString(3, state.getOrigin());
            return ps;
        });
        log.debug("State saved");
        GeneratedKeyHolder generatedKeyHolder = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(queryReceipt, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, receipt.getCardNumber());
            ps.setTimestamp(2, new Timestamp(receipt.getDate()));
            ps.setBigDecimal(3, receipt.getSum());
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
        String sql = "select total_count, code, (select name from Products where Products.code=R.code limit 1) as name from (select SUM(count) as total_count, code from (select code, name, count from Receipts INNER JOIN Products on Products.sale_id=Receipts.sale_id where card_number=?) as T GROUP BY T.code order by total_count desc limit ?) as R";
        List<FavouriteProduct> results = jdbc.query(sql,
                new Object[]{card, limit},
                (rs, rowNum) -> new FavouriteProduct(rs.getString("name"),
                        rs.getInt("total_count"),
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
        String sql = "select sum(Receipts.sum) AS totalSum from Receipts WHERE sale_date>=? and sale_date<?";
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

    @Override
    @Transactional
    public DBState prepareStateForOrigin(String origin) {
        List<DBState> originCheckpoint = jdbc.query("select offset, origin, processed from States where origin=?", new Object[]{origin},
                (ResultSet rs, int rowNum) -> new DBState(rs.getString("origin"), rs.getInt("offset"), rs.getBoolean("processed")));
        if (originCheckpoint.isEmpty()) {
            log.debug("New origin:{}. Start from the beginning", origin);
            DBState state = new DBState(origin, -1, false);
            jdbc.update(con -> {
                PreparedStatement ps = con.prepareStatement("insert into States(offset, origin, processed) values (?,?,?)");
                ps.setInt(1, state.getOffset());
                ps.setString(2, state.getOrigin());
                ps.setBoolean(3, state.isProcessed());
                return ps;
            });
            return state;
        } else if (originCheckpoint.size() == 1) {
            DBState state = originCheckpoint.get(0);
            log.debug("Resume import from state={}", state);
            return state;
        } else {
            throw new RuntimeException("Corrupted DB State: non unique state for origin:" + origin);
        }
    }
}
