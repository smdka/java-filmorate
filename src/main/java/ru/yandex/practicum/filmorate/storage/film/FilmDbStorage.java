package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

@Repository
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Collection<Film> findAll() {
        String sql = "SELECT FILMS.ID, " +
                            "FILMS.NAME, " +
                            "FILMS.DESCRIPTION, " +
                            "FILMS.RELEASE_DATE, " +
                            "FILMS.DURATION," +
                            "FILMS.MPA_ID, " +
                            "MPA.NAME AS MPA_NAME " +
                     "FROM FILMS " +
                     "INNER JOIN MPA on FILMS.MPA_ID = MPA.ID";
        return jdbcTemplate.query(sql, this::mapRowToFilm);
    }

    @Override
    public Film save(Film film) {
        String sql = "INSERT INTO FILMS (NAME, DESCRIPTION, RELEASE_DATE, DURATION, MPA_ID) " +
                     "VALUES (?, ?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sql, new String[] {"ID"});
            stmt.setString(1, film.getName());
            stmt.setString(2, film.getDescription());
            stmt.setDate(3, Date.valueOf(film.getReleaseDate()));
            stmt.setInt(4, film.getDuration());
            stmt.setInt(5, film.getMpa().getId());
            return stmt;
        }, keyHolder);

        int filmId = keyHolder.getKey().intValue();
        film.setId(filmId);

        saveGenres(film);
        saveLikes(film);
        return film;
    }

    private void saveGenres(Film film) {
        jdbcTemplate.update("DELETE FROM FILM_GENRE WHERE FILM_ID = ?", film.getId());

        Set<Genre> genres = film.getGenres();
        int filmId = film.getId();
        String sql = "INSERT INTO FILM_GENRE (FILM_ID, GENRE_ID) VALUES (?, ?)";

        if (genres != null) {
            for(Genre genre : genres) {
                jdbcTemplate.update(sql, filmId, genre.getId());
            }
        }
    }

    @Override
    public Optional<Film> update(Film film) {
        String sql = "UPDATE FILMS SET " +
                     "NAME = ?, DESCRIPTION = ?, RELEASE_DATE = ?, DURATION = ?, MPA_ID = ? " +
                     "WHERE ID = ?";
        if (jdbcTemplate.update(sql, film.getName(),
                film.getDescription(),
                Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId()) == 0) {
            return Optional.empty();
        }

        saveGenres(film);
        saveLikes(film);
        return Optional.of(film);
    }

    private void saveLikes(Film film) {
        jdbcTemplate.update("DELETE FROM FILM_LIKES WHERE FILM_ID = ?", film.getId());

        Set<Integer> whoLikedUserIds = film.getWhoLikedUserIds();

        String sql = "INSERT INTO FILM_LIKES (FILM_ID, LIKED_BY_USER_ID) VALUES (?, ?)";
        int filmId = film.getId();

        if (whoLikedUserIds != null) {
            for(int id : whoLikedUserIds) {
                jdbcTemplate.update(sql, filmId, id);
            }
        }
    }

    @Override
    public boolean deleteById(int filmId) {
        return jdbcTemplate.update("DELETE FROM FILMS WHERE ID = ?", filmId) > 0;
    }

    @Override
    public Optional<Film> findById(int id) {
        String sql = "SELECT FILMS.ID, " +
                            "FILMS.NAME, " +
                            "FILMS.DESCRIPTION, " +
                            "FILMS.RELEASE_DATE, " +
                            "FILMS.DURATION," +
                            "FILMS.MPA_ID, " +
                            "MPA.NAME AS MPA_NAME " +
                     "FROM FILMS " +
                     "INNER JOIN MPA on FILMS.MPA_ID = MPA.ID " +
                     "WHERE FILMS.ID = ?";
        List<Film> results = jdbcTemplate.query(sql, this::mapRowToFilm, id);
        return results.isEmpty() ?
                Optional.empty() :
                Optional.of(results.get(0));
    }

    private Film mapRowToFilm(ResultSet row, int rowNum) throws SQLException {
        int id = row.getInt("ID");
        String name  = row.getString("NAME");
        String description = row.getString("DESCRIPTION");
        LocalDate releaseDate = row.getDate("RELEASE_DATE").toLocalDate();
        int duration = row.getInt("DURATION");
        Mpa mpa = new Mpa(row.getInt("MPA_ID"), row.getString("MPA_NAME"));
        Set<Genre> genres = getGenresByFilmId(id);
        Set<Integer> likes = getWhoLikedIdsByFilmId(id);
        return new Film(id, likes, name, description, releaseDate, duration, mpa, genres);
    }

    private Set<Integer> getWhoLikedIdsByFilmId(int id) {
        String sql = "SELECT LIKED_BY_USER_ID FROM FILM_LIKES " +
                     "WHERE FILM_ID = ?";
        return new HashSet<>(jdbcTemplate.query(sql, (rs, rowNum) -> rs.getInt("LIKED_BY_USER_ID"), id));
    }

    private Set<Genre> getGenresByFilmId(int id) {
        String sql = "SELECT ID, NAME FROM GENRES " +
                     "INNER JOIN FILM_GENRE FG on GENRES.ID = FG.GENRE_ID " +
                     "WHERE FILM_ID = ?";
        return new TreeSet<>(jdbcTemplate.query(sql, this::mapRowToGenre, id));
    }

    private Genre mapRowToGenre(ResultSet rs, int rowNum) throws SQLException {
        return new Genre(rs.getInt("ID"), rs.getString("NAME"));
    }
}
