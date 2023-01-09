package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

@Repository
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public FilmDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    @Override
    public Collection<Film> findAll() {
        return jdbcTemplate.query(
                "SELECT ID, TITLE, DESCRIPTION, RELEASE_DATE, DURATION, MPA_RATING, LIKED_BY_USER_ID, GENRE_ID " +
                    "FROM FILMS " +
                    "JOIN FILM_LIKES FL on FILMS.ID = FL.FILM_ID " +
                    "JOIN FILM_GENRE FG on FILMS.ID = FG.FILM_ID " +
                    "GROUP BY FILMS.ID",
                this::mapRowToFilm);
    }

    @Override
    public int add(Film film) {
        String sql = "INSERT INTO FILMS (TITLE, DESCRIPTION, RELEASE_DATE, DURATION, MPA_RATING) " +
                     "VALUES (?, ?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sql, new String[] {"ID"});
            stmt.setString(1, film.getTitle());
            stmt.setString(2, film.getDescription());
            stmt.setDate(3, Date.valueOf(film.getReleaseDate()));
            stmt.setInt(4, film.getDuration());
            stmt.setInt(5, film.getMpaRatingId());
            return stmt;
        }, keyHolder);
        return keyHolder.getKey().intValue();
    }

    @Override
    public boolean update(Film film) {
        String sql = "UPDATE FILMS SET " +
                     "TITLE = ?, DESCRIPTION = ?, RELEASE_DATE = ?, DURATION = ?, MPA_RATING = ? " +
                     "WHERE ID = ?";
        return jdbcTemplate.update(sql,
                            film.getTitle(),
                            film.getDescription(),
                            Date.valueOf(film.getReleaseDate()),
                            film.getDuration(),
                            film.getMpaRatingId()) > 0;
    }

    @Override
    public boolean delete(int filmId) {
        String sql = "DELETE FROM FILMS " +
                     "WHERE ID = ?";
        return jdbcTemplate.update(sql, filmId) > 0;
    }

    @Override
    public List<Film> getTopN(int n, Comparator<Film> comparator) {
        return null;
    }

    @Override
    public Optional<Film> getFilmById(int id) {
        List<Film> results = jdbcTemplate.query(
                "SELECT ID, TITLE, DESCRIPTION, RELEASE_DATE, DURATION, MPA_RATING, LIKED_BY_USER_ID, GENRE_ID " +
                    "FROM FILMS " +
                    "JOIN FILM_GENRE FG on FILMS.ID = FG.FILM_ID " +
                    "JOIN FILM_LIKES FL on FILMS.ID = FL.FILM_ID " +
                    "WHERE FILMS.ID=? " +
                    "GROUP BY FILMS.ID",
                this::mapRowToFilm,
                id);
        return results.size() == 0 ?
                Optional.empty() :
                Optional.of(results.get(0));
    }

    private Film mapRowToFilm(ResultSet row, int rowNum) throws SQLException {
        int id = row.getInt("ID");
        String title = row.getString("TITLE");
        String description = row.getString("DESCRIPTION");
        LocalDate releaseDate = row.getDate("RELEASE_DATE").toLocalDate();
        int duration = row.getInt("DURATION");
        int mpaRatingId = row.getInt("MPA_RATING");
        Set<Integer> genreIds = Set.of((Integer[]) row.getArray("GENRE_ID").getArray());
        Set<Integer> whoLikedIds = Set.of((Integer[]) row.getArray("LIKED_BY_USER_ID").getArray());
        return new Film(id, whoLikedIds, title, description, releaseDate, duration, mpaRatingId, genreIds);
    }
}
