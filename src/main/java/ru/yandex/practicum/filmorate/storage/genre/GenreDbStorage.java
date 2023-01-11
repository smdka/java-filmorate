package ru.yandex.practicum.filmorate.storage.genre;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class GenreDbStorage {
    private final JdbcTemplate jdbcTemplate;


    public GenreDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Genre> findAll() {
        return jdbcTemplate.query("SELECT * FROM GENRES", this::mapRowToGenre);
    }

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
