package net.ostrekalovsky.rat.service.storage;

import lombok.Data;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SingleColumnRowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Data
public class DBState {

    private final String origin;
    private final String lastSavedCardNumber;
    private final long lastSavedSaleDate;

    public static Optional<DBState> valueOf(JdbcTemplate jdbc, String origin) {
        List<DBState> originCheckpoint = jdbc.query("select card_number, sale_date from States where origin=?", new Object[]{origin},
                getDbStateRowMapper(origin));
        if (originCheckpoint.isEmpty()) {
            return Optional.empty();
        } else if (origin.length() == 1) {
            return Optional.of(originCheckpoint.get(0));
        } else {
            throw new RuntimeException("Corrupted DB State: non unique state for origin:" + origin);
        }
    }

    private static RowMapper<DBState> getDbStateRowMapper(String origin) {
        return new RowMapper<DBState>() {
            @Override
            public DBState mapRow(ResultSet rs, int rowNum) throws SQLException {
                return new DBState(
                        origin,
                        rs.getString("card_number"),
                        rs.getTimestamp("sale_date ").getTime()
                );
            }
        };
    }
}
