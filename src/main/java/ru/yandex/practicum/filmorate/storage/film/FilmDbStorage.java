package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;

@Component
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {
    private static final String FIND_ALL =
            "SELECT FILMS.*, " +
                    "MPA.NAME AS MPA_NAME, " +
                    "YEAR(RELEASE_DATE) AS YEAR_RELEASE, " +
                    "ARRAY_AGG(FG.GENRE_ID) AS GENRE_IDS, " +
                    "ARRAY_AGG(G.NAME) AS GENRE_NAMES, " +
                    "ARRAY_AGG(FL.LIKED_BY_USER_ID) AS LIKES," +
                    "ARRAY_AGG(FD.DIRECTOR_ID) AS DIRECTOR_IDS," +
                    "ARRAY_AGG(D.NAME) AS DIRECTOR_NAMES," +
                    "FROM FILMS " +
                    "LEFT JOIN MPA on FILMS.MPA_ID = MPA.ID " +
                    "LEFT JOIN FILM_GENRE FG on FILMS.ID = FG.FILM_ID " +
                    "LEFT JOIN GENRES G on G.ID = FG.GENRE_ID " +
                    "LEFT JOIN FILM_LIKES FL on FILMS.ID = FL.FILM_ID " +
                    "LEFT JOIN FILM_DIRECTOR FD on FILMS.ID = FD.FILM_ID " +
                    "LEFT JOIN DIRECTORS D on D.ID = FD.DIRECTOR_ID ";
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Collection<Film> findAll() {
        String sql = FIND_ALL +
                "GROUP BY FILMS.ID";
        return jdbcTemplate.query(sql, this::mapRowToFilm);
    }

    private Film mapRowToFilm(ResultSet rs, int rowNum) throws SQLException {
        int id = rs.getInt("ID");
        String name  = rs.getString("NAME");
        String description = rs.getString("DESCRIPTION");
        LocalDate releaseDate = rs.getDate("RELEASE_DATE").toLocalDate();
        int duration = rs.getInt("DURATION");
        Mpa mpa = new Mpa(rs.getInt("MPA_ID"), rs.getString("MPA_NAME"));
        SortedSet<Genre> genres = getSetOfGenres(rs);
        SortedSet<Director> director = getSetOfDirector(rs);
        Set<Integer> likes = getSetOfLikes(rs);
        return new Film(id, likes, name, description, releaseDate, duration, mpa, director, genres);
    }

    private SortedSet<Genre> getSetOfGenres(ResultSet row) throws SQLException {
        Array idsArr = row.getArray("GENRE_IDS");
        Array namesArr = row.getArray("GENRE_NAMES");
        Object[] idsArrValues = (Object[]) idsArr.getArray();
        if (idsArrValues[0] == null) {
            return Collections.emptySortedSet();
        }
        Object[] namesArrValues = (Object[]) namesArr.getArray();
        SortedSet<Genre> result = new TreeSet<>();
        for (int i = 0; i < idsArrValues.length; i++) {
            result.add(new Genre((Integer) idsArrValues[i], (String) namesArrValues[i]));
        }
        return result;
    }

    private SortedSet<Director> getSetOfDirector(ResultSet row) throws SQLException {
        Array idsArr = row.getArray("DIRECTOR_IDS");
        Array namesArr = row.getArray("DIRECTOR_NAMES");
        Object[] idsArrValues = (Object[]) idsArr.getArray();
        if (idsArrValues[0] == null) {
            return Collections.emptySortedSet();
        }
        Object[] namesArrValues = (Object[]) namesArr.getArray();
        SortedSet<Director> result = new TreeSet<>();
        for (int i = 0; i < idsArrValues.length; i++) {
            result.add(new Director((Integer) idsArrValues[i], (String) namesArrValues[i]));
        }
        return result;
    }

    private Set<Integer> getSetOfLikes(ResultSet rs) throws SQLException {
        Array likesArray = rs.getArray("LIKES");
        Object[] values = (Object[]) likesArray.getArray();
        if (values[0] == null) {
            return Collections.emptySet();
        }
        return Arrays.stream(values)
                .map(value -> (Integer) value)
                .collect(toSet());
    }

    @Override
    public Film save(Film film) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String SQL_ADD_FILM = "INSERT INTO FILMS (NAME, DESCRIPTION, RELEASE_DATE, DURATION, MPA_ID) " +
                "VALUES (?, ?, ?, ?, ?)";
        jdbcTemplate.update(con -> {
            PreparedStatement preparedStatement = con.prepareStatement(SQL_ADD_FILM, new String[]{"ID"});
            preparedStatement.setString(1, film.getName());
            preparedStatement.setString(2, film.getDescription());
            preparedStatement.setDate(3, Date.valueOf(film.getReleaseDate()));
            preparedStatement.setInt(5, film.getMpa().getId());
            preparedStatement.setLong(4, film.getDuration());
            return preparedStatement;
        }, keyHolder);
        Integer filmId = keyHolder.getKey().intValue();
        film.setId(filmId);

        saveGenres(film);
        saveDirectors(film);
        return film;
    }

    private void saveGenres(Film film) {
        jdbcTemplate.update("DELETE FROM FILM_GENRE WHERE FILM_ID = ?", film.getId());

        SortedSet<Genre> genres = film.getGenres();
        int filmId = film.getId();
        String sql = "INSERT INTO FILM_GENRE (FILM_ID, GENRE_ID) VALUES (?, ?)";

        if (!genres.isEmpty()) {
            for (Genre genre : genres) {
                jdbcTemplate.update(sql, filmId, genre.getId());
            }
        }
    }

    private void saveDirectors(Film film) {
        jdbcTemplate.update("DELETE FROM FILM_DIRECTOR WHERE FILM_ID = ?", film.getId());
        SortedSet<Director> directors  = film.getDirectors();
        int filmId = film.getId();
        String sql = "INSERT INTO FILM_DIRECTOR(DIRECTOR_ID, FILM_ID) VALUES (?,?) ";

        if (!directors.isEmpty()) {
            for (Director director : directors) {
                jdbcTemplate.update(sql, director.getId(), filmId);
            }
        }
    }

    @Override
    public Optional<Film> update(Film film) {
        String sql = "UPDATE FILMS SET " +
                "NAME = ?, DESCRIPTION = ?, RELEASE_DATE = ?, DURATION = ?, MPA_ID = ? " +
                "WHERE ID = ?";
        if (
                jdbcTemplate.update(sql,
                        film.getName(),
                        film.getDescription(),
                        Date.valueOf(film.getReleaseDate()),
                        film.getDuration(),
                        film.getMpa().getId(),
                        film.getId()) == 0)
        {
            return Optional.empty();
    }

        saveGenres(film);
        saveLikes(film);
        saveDirectors(film);
        return Optional.of(film);
    }

    private void saveLikes(Film film) {
        jdbcTemplate.update("DELETE FROM FILM_LIKES WHERE FILM_ID = ?", film.getId());

        Set<Integer> whoLikedUserIds = film.getWhoLikedUserIds();

        String sql = "INSERT INTO FILM_LIKES (FILM_ID, LIKED_BY_USER_ID) VALUES (?, ?)";
        int filmId = film.getId();

        if (!whoLikedUserIds.isEmpty()) {
            for (int id : whoLikedUserIds) {
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
        String sql = FIND_ALL +
                "WHERE FILMS.ID = ? " +
                "GROUP BY FILMS.ID";
        List<Film> results = jdbcTemplate.query(sql, this::mapRowToFilm, id);
        return results.isEmpty() ?
                Optional.empty() :
                Optional.of(results.get(0));
    }

    @Override
    public List<Film> getFilmsByDirector (int directorId, String sortBy) {
        String sql = FIND_ALL +
                "LEFT JOIN (SELECT DIRECTORS.*, " +
                "F.ID AS FILM_ID," +
                "YEAR(F.RELEASE_DATE) AS YEARS, " +
                "COUNT(FL.FILM_ID) AS LIKES " +
                "FROM DIRECTORS " +
                "LEFT JOIN FILM_DIRECTOR FD on DIRECTORS.ID = FD.DIRECTOR_ID " +
                "LEFT JOIN FILMS F on F.ID = FD.FILM_ID " +
                "LEFT JOIN FILM_LIKES FL on F.ID = FL.FILM_ID " +
                "WHERE DIRECTORS.ID = ? " +
                "GROUP BY FD.FILM_ID) AS DF " +
                "WHERE FILMS.ID = DF.FILM_ID " +
                "GROUP BY FILMS.ID " +
                "ORDER BY " + sortBy + " DESC";
        return jdbcTemplate.query(sql, this::mapRowToFilm, directorId);
    }

    @Override
    public Collection<Film> findTopNMostPopular(int n) {
        String sql = FIND_ALL +
                "GROUP BY FILMS.ID " +
                "ORDER BY COUNT(DISTINCT FL.LIKED_BY_USER_ID) DESC " +
                "LIMIT ?";
        return jdbcTemplate.query(sql, this::mapRowToFilm, n);
    }

    @Override
    public boolean addLike(int filmId, int userId) {
        String sql = "MERGE INTO FILM_LIKES(FILM_ID, LIKED_BY_USER_ID) VALUES (?, ?)";
        return jdbcTemplate.update(sql, filmId, userId) > 0;
    }

    @Override
    public boolean deleteLike(int filmId, int userId) {
        String sql = "DELETE FROM FILM_LIKES " +
                "WHERE FILM_ID = ? AND LIKED_BY_USER_ID = ?";
        return jdbcTemplate.update(sql, filmId, userId) > 0;
    }
}