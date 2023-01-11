package ru.yandex.practicum.filmorate.storage.mpa;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class MpaDbStorage {
    private final JdbcTemplate jdbcTemplate;

    public MpaDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Mpa> findAll() {
        return jdbcTemplate.query("SELECT * FROM MPA", this::mapRowToMpa);
    }

    private Mpa mapRowToMpa(ResultSet rs, int i) throws SQLException {
        return new Mpa(rs.getInt("ID"), rs.getString("NAME"));
    }

    public Optional<Mpa> findById(int id) {
        String sql = "SELECT * FROM MPA WHERE ID = ?";
        List<Mpa> results = jdbcTemplate.query(sql, this::mapRowToMpa, id);
        return results.isEmpty() ?
                Optional.empty() :
                Optional.of(results.get((0)));
    }
}