package ru.yandex.practicum.filmorate.storage.genre;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class GenreDbStorage implements GenreStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Collection<Genre> findAll() {
        return jdbcTemplate.query("SELECT * FROM GENRES", this::mapRowToGenre);
    }

    @Override
    public Optional<Genre> findById(int id) {
        String sql = "SELECT * FROM GENRES WHERE ID = ?";
        List<Genre> results = jdbcTemplate.query(sql, this::mapRowToGenre, id);
        return results.isEmpty() ?
                Optional.empty() :
                Optional.of(results.get((0)));
    }

    private Genre mapRowToGenre(ResultSet rs, int rowNum) throws SQLException {
        return new Genre(rs.getInt("ID"), rs.getString("NAME"));
    }
}
