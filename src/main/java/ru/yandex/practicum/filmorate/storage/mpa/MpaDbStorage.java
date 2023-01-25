package ru.yandex.practicum.filmorate.storage.mpa;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MpaDbStorage implements MpaStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Collection<Mpa> findAll() {
        return jdbcTemplate.query("SELECT * FROM MPA", (rs, i) -> mapRowToMpa(rs));
    }

    private Mpa mapRowToMpa(ResultSet rs) throws SQLException {
        return new Mpa(rs.getInt("ID"), rs.getString("NAME"));
    }

    @Override
    public Optional<Mpa> findById(int id) {
        String sql = "SELECT * FROM MPA WHERE ID = ?";
        List<Mpa> results = jdbcTemplate.query(sql, (rs, i) -> mapRowToMpa(rs), id);
        return results.isEmpty() ?
                Optional.empty() :
                Optional.of(results.get((0)));
    }
}
