package net.ostrekalovsky.rat.service.storage;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

@Slf4j
@Data
public class DBState {

    private final String origin;
    private int offset;

    public DBState(String origin, int offset) {
        this.origin = origin;
        this.offset = offset;
    }

    public void moveForward() {
        offset++;
    }

    public static DBState valueOf(JdbcTemplate jdbc, String origin) {
        List<DBState> originCheckpoint = jdbc.query("select offset, origin from States where origin=?", new Object[]{origin},
                (ResultSet rs, int rowNum) -> new DBState(rs.getString("origin"), rs.getInt("offset")));
        if (originCheckpoint.isEmpty()) {
            log.debug("New origin:{}. Start from the beginning", origin);
            DBState state = new DBState(origin, -1);
            jdbc.update(con -> {
                PreparedStatement ps = con.prepareStatement("insert into States(offset, origin) values (?,?)");
                ps.setInt(1, state.getOffset());
                ps.setString(2, state.getOrigin());
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
