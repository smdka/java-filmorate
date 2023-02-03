package ru.yandex.practicum.filmorate.storage.event;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.FeedEvent;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Operation;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class FeedEventDbStorage implements FeedEventStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void save(int userId, EventType eventType, Operation operation, int entityId) {
        String sql =
                "INSERT INTO USER_FEEDS " +
                "(USER_ID, TIME_STAMP, EVENT_TYPE, OPERATION, ENTITY_ID) " +
                "VALUES (?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, userId, Instant.now().toEpochMilli(), eventType.toString(), operation.toString(),
                entityId);
    }

    @Override
    public List<FeedEvent> findAllByUserId(int id) {
        String sql = "SELECT * FROM USER_FEEDS WHERE USER_ID = ?";
        return jdbcTemplate.query(sql, this::mapRowToFeed, id);
    }

    private FeedEvent mapRowToFeed(ResultSet rs, int rowNum) throws SQLException {
        return FeedEvent.builder()
                .eventId(rs.getInt("EVENT_ID"))
                .userId(rs.getInt("USER_ID"))
                .timestamp(rs.getLong("TIME_STAMP"))
                .eventType(rs.getString("EVENT_TYPE"))
                .operation(rs.getString("OPERATION"))
                .entityId(rs.getInt("ENTITY_ID"))
                .build();
    }
}
