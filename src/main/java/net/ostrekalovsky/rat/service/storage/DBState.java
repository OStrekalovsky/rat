package net.ostrekalovsky.rat.service.storage;

import lombok.Data;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SingleColumnRowMapper;

import java.util.List;
import java.util.Optional;

@Data
public class DBState {

    private final String fileName;
    private final String lastSavedCardNumber;
    private final long lastSavedSaleDate;

    public static Optional<DBState> valueOf(JdbcTemplate jdbc, String origin) {
        List<DBState> originCheckpoint = jdbc.query("select (card_number, sale_date) from State where origin=?", new Object[]{origin}, new SingleColumnRowMapper<>());
        if (origin.isEmpty()) {
            return Optional.empty();
        } else if (origin.length() == 1) {
            return Optional.of(originCheckpoint.get(0));
        } else {
            throw new RuntimeException("Corrupted DB State: non unique state for origin:" + origin);
        }
    }
}
