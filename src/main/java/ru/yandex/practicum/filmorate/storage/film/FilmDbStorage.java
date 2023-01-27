package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.utilities.recommendations.Recommender;
import ru.yandex.practicum.filmorate.utilities.recommendations.Matrix;
import ru.yandex.practicum.filmorate.utilities.sql.SqlArrayConverter;

import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;

import static java.util.stream.Collectors.*;

@Repository
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {
    private static final String FIND_ALL =
            "SELECT FILMS.*, " +
                   "MPA.NAME AS MPA_NAME, " +
                   "ARRAY_AGG(FG.GENRE_ID) AS GENRE_IDS, " +
                   "ARRAY_AGG(G.NAME) AS GENRE_NAMES, " +
                   "ARRAY_AGG(FL.LIKED_BY_USER_ID) AS WHO_LIKED_IDS," +
            "FROM FILMS " +
            "LEFT JOIN MPA on FILMS.MPA_ID = MPA.ID " +
            "LEFT JOIN FILM_GENRE FG on FILMS.ID = FG.FILM_ID " +
            "LEFT JOIN GENRES G on G.ID = FG.GENRE_ID " +
            "LEFT JOIN FILM_LIKES FL on FILMS.ID = FL.FILM_ID ";
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Collection<Film> findAll() {
        String sql = FIND_ALL +
                    "GROUP BY FILMS.ID";
        return jdbcTemplate.query(sql, (rs, rowNum) -> mapRowToFilm(rs));
    }

    private Film mapRowToFilm(ResultSet rs) throws SQLException {
        int id = rs.getInt("ID");
        String name  = rs.getString("NAME");
        String description = rs.getString("DESCRIPTION");
        LocalDate releaseDate = rs.getDate("RELEASE_DATE").toLocalDate();
        int duration = rs.getInt("DURATION");
        Mpa mpa = new Mpa(rs.getInt("MPA_ID"), rs.getString("MPA_NAME"));
        SortedSet<Genre> genres = getSetOfGenres(rs);
        Set<Integer> likes = getSetOfLikes(rs);
        return new Film(id, likes, name, description, releaseDate, duration, mpa, genres);
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

    private Set<Integer> getSetOfLikes(ResultSet rs) throws SQLException {
        Array likesArray = rs.getArray("WHO_LIKED_IDS");
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

        if (!whoLikedUserIds.isEmpty()) {
            for (int id : whoLikedUserIds) {
                jdbcTemplate.update(sql, filmId, id);
            }
        }
    }

    @Override
    public boolean deleteById(int id) {
        return jdbcTemplate.update("DELETE FROM FILMS WHERE ID = ?", id) > 0;
    }

    @Override
    public Optional<Film> findById(int id) {
        String sql = FIND_ALL +
                    "WHERE FILMS.ID = ? " +
                    "GROUP BY FILMS.ID";
        List<Film> results = jdbcTemplate.query(sql, (rs, rowNum) -> mapRowToFilm(rs), id);
        return results.isEmpty() ?
                Optional.empty() :
                Optional.of(results.get(0));
    }

    @Override
    public Collection<Film> findTopNMostPopular(int n) {
        String sql = FIND_ALL +
                    "GROUP BY FILMS.ID " +
                    "ORDER BY COUNT(DISTINCT FL.LIKED_BY_USER_ID) DESC " +
                    "LIMIT ?";
        return jdbcTemplate.query(sql, (rs, rowNum) -> mapRowToFilm(rs), n);
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

    @Override
    public Collection<Film> getRecommendations(int userId) {
        //first query to make recommendations
        String sql1 =
                "SELECT fl.film_id film_ids, " +
                        "ARRAY_AGG(fl.liked_by_user_id) user_ids, " +
                        "ARRAY_AGG(fl.liked_by_user_id/fl.liked_by_user_id) rates " +
                        "FROM FILM_LIKES AS FL " +
                        "GROUP BY FL.film_id";
        Matrix matrix = jdbcTemplate.query(sql1, this::makeMatrixForRecommendations);
        Recommender recommender = new Recommender(matrix);
        List<Integer> recommendations = recommender.getRecommendations(userId, Optional.empty());
        //second query for recommended films if necessary
        if (!recommendations.isEmpty()) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("(");
            for (Integer film_id : recommendations) {
                stringBuilder.append(film_id + ", ");
            }
            String sql2 = FIND_ALL +
                    "WHERE FILMS.ID IN " +
                    stringBuilder.substring(0, stringBuilder.length() - 2) + ") GROUP BY FILMS.ID";
            return jdbcTemplate.query(sql2, (rs, rowNum) -> mapRowToFilm(rs));
        } else {
            return List.of();
        }
    }

    private Matrix makeMatrixForRecommendations (ResultSet rs) throws SQLException {
        Matrix data = new Matrix();
        try {
            SqlArrayConverter converter = new SqlArrayConverter();
            while (rs.next()) {
                List<Integer> user_ids = converter.convert(rs.getArray("user_ids"));
                List<Integer> rates = converter.convert(rs.getArray("rates"));
                for (int i = 0; i < user_ids.size(); i++) {
                    data.writeValue(rs.getInt("film_ids"), user_ids.get(i), Optional.of(rates.get(i).doubleValue()));
                }
            }
            return data;
        } catch (EmptyResultDataAccessException exp) {
            return null;
        }
    }
}
