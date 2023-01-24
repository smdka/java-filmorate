package ru.yandex.practicum.filmorate.storage.director;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.*;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

@Component
@RequiredArgsConstructor
public class DirectorDbStorage implements DirectorStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Director> getAll() {
        return jdbcTemplate.query("SELECT * FROM DIRECTORS", this::mapRowToDirector);
    }

    @Override
    public Director findById(int id) {
        String sql = "SELECT * FROM DIRECTORS WHERE ID = ?";
        List<Director> results = jdbcTemplate.query(sql, this::mapRowToDirector, id);
        return results.get(0);
    }
    @Override
    public Director addDirector(Director director){
        String sql = "INSERT INTO DIRECTORS (NAME)" +
                "VALUES (?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sql, new String[] {"ID"});
            stmt.setString(1, director.getName());
            return stmt;
        }, keyHolder);
                int directorId = keyHolder.getKey().intValue();
        director.setId(directorId);
        return director;
    }

    @Override
    public boolean deleteById (int id) {
        String sql = "DELETE FROM DIRECTORS " +
                "WHERE ID = ?";
        return jdbcTemplate.update(sql, id) > 0;
    }

    @Override
    public Director update (Director director) {
        String sql = "UPDATE DIRECTORS " +
                "SET NAME = ?" +
                "WHERE ID = ?";
        System.out.println(director.getId());
        System.out.println(director.getName());
        jdbcTemplate.update(sql,
                director.getName(),
                director.getId());

        return director;
    }

    private Director mapRowToDirector(ResultSet rs, int i) throws SQLException {
        return new Director(rs.getInt("ID"), rs.getString("NAME"));
    }
}
