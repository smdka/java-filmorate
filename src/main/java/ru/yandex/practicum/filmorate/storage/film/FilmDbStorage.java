package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.model.SortBy;
import ru.yandex.practicum.filmorate.utilities.recommendations.Recommender;
import ru.yandex.practicum.filmorate.utilities.recommendations.Matrix;
import ru.yandex.practicum.filmorate.utilities.sql.SqlArrayConverter;

import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.*;

@Component
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {
    private static final String FIND_ALL =
            "SELECT FILMS.*, " +
                    "MPA.NAME AS MPA_NAME, " +
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
        return jdbcTemplate.query(sql, (rs, rowNum) -> mapRowToFilm(rs));
    }

    private Film mapRowToFilm(ResultSet rs) throws SQLException {
        int id = rs.getInt("ID");
        String name = rs.getString("NAME");
        String description = rs.getString("DESCRIPTION");
        LocalDate releaseDate = rs.getDate("RELEASE_DATE").toLocalDate();
        int duration = rs.getInt("DURATION");
        Mpa mpa = new Mpa(rs.getInt("MPA_ID"), rs.getString("MPA_NAME"));
        Set<Genre> genres = getSetOfGenres(rs);
        Set<Director> director = getSetOfDirector(rs);
        Set<Integer> likes = getSetOfLikes(rs);
        return new Film(id, likes, name, description, releaseDate, duration, mpa, director, genres);
    }

    private Set<Genre> getSetOfGenres(ResultSet row) throws SQLException {
        Array idsArr = row.getArray("GENRE_IDS");
        Array namesArr = row.getArray("GENRE_NAMES");
        Object[] idsArrValues = (Object[]) idsArr.getArray();
        if (idsArrValues[0] == null) {
            return Collections.emptySet();
        }
        Object[] namesArrValues = (Object[]) namesArr.getArray();
        return IntStream.range(0, idsArrValues.length)
                .mapToObj(i -> new Genre((Integer) idsArrValues[i], (String) namesArrValues[i]))
                .sorted(Comparator.comparingInt(Genre::getId))
                .collect(toCollection(LinkedHashSet::new));
    }

    private Set<Director> getSetOfDirector(ResultSet row) throws SQLException {
        Array idsArr = row.getArray("DIRECTOR_IDS");
        Array namesArr = row.getArray("DIRECTOR_NAMES");
        Object[] idsArrValues = (Object[]) idsArr.getArray();
        if (idsArrValues[0] == null) {
            return Collections.emptySortedSet();
        }
        Object[] namesArrValues = (Object[]) namesArr.getArray();
        return IntStream.range(0, idsArrValues.length)
                .mapToObj(i -> new Director((Integer) idsArrValues[i], (String) namesArrValues[i]))
                .collect(toSet());
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
        String sql = "INSERT INTO FILMS (NAME, DESCRIPTION, RELEASE_DATE, DURATION, MPA_ID) " +
                "VALUES (?, ?, ?, ?, ?)";
        jdbcTemplate.update(connection -> {
            PreparedStatement stm = connection.prepareStatement(sql, new String[]{"ID"});
            stm.setString(1, film.getName());
            stm.setString(2, film.getDescription());
            stm.setDate(3, Date.valueOf(film.getReleaseDate()));
            stm.setInt(5, film.getMpa().getId());
            stm.setLong(4, film.getDuration());
            return stm;
        }, keyHolder);
        int filmId = keyHolder.getKey().intValue();
        film.setId(filmId);

        saveGenres(film);
        saveDirectors(film);
        setGenresSortedById(film);
        return film;
    }

    private void setGenresSortedById(Film film) {
        Set<Genre> genres = film.getGenres().stream()
                .sorted(Comparator.comparingInt(Genre::getId))
                .collect(Collectors.toCollection(LinkedHashSet::new));
        film.setGenres(genres);
    }

    private void saveGenres(Film film) {
        jdbcTemplate.update("DELETE FROM FILM_GENRE WHERE FILM_ID = ?", film.getId());

        List<Genre> genres = new ArrayList<>(film.getGenres());
        int filmId = film.getId();
        String sql = "INSERT INTO FILM_GENRE (FILM_ID, GENRE_ID) VALUES (?, ?)";

        if (!genres.isEmpty()) {
            jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    ps.setInt(1, filmId);
                    ps.setInt(2, genres.get(i).getId());
                }

                @Override
                public int getBatchSize() {
                    return genres.size();
                }
            });
        }
    }

    @Override
    public Optional<Film> update(Film film) {
        String sql = "UPDATE FILMS SET " +
                "NAME = ?, DESCRIPTION = ?, RELEASE_DATE = ?, DURATION = ?, MPA_ID = ? " +
                "WHERE ID = ?";

        if (jdbcTemplate.update(sql,
                        film.getName(),
                        film.getDescription(),
                        Date.valueOf(film.getReleaseDate()),
                        film.getDuration(),
                        film.getMpa().getId(),
                        film.getId()) == 0) {
            return Optional.empty();
        }

        saveGenres(film);
        saveLikes(film);
        saveDirectors(film);
        setGenresSortedById(film);
        return Optional.of(film);
    }

    private void saveLikes(Film film) {
        jdbcTemplate.update("DELETE FROM FILM_LIKES WHERE FILM_ID = ?", film.getId());

        List<Integer> whoLikedUserIds = new ArrayList<>(film.getWhoLikedUserIds());

        String sql = "INSERT INTO FILM_LIKES (FILM_ID, LIKED_BY_USER_ID) VALUES (?, ?)";
        int filmId = film.getId();

        if (!whoLikedUserIds.isEmpty()) {
            jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    ps.setInt(1, filmId);
                    ps.setInt(2, whoLikedUserIds.get(i));
                }

                @Override
                public int getBatchSize() {
                    return whoLikedUserIds.size();
                }
            });
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
        List<Film> results = jdbcTemplate.query(sql, (rs, rowNum) -> mapRowToFilm(rs), id);
        return results.isEmpty() ?
                Optional.empty() :
                Optional.of(results.get(0));
    }

    @Override
    public Collection<Film> findTopNMostPopular(int limit, Optional<Integer> genreId, Optional<Integer> year) {
        String sql;
        String yearSql = "WHERE EXTRACT(YEAR from CAST(RELEASE_DATE as date)) = ? ";
        String groupBySql = "GROUP BY FILMS.ID ";
        String genreIdSql = "HAVING ARRAY_CONTAINS(GENRE_IDS, ?) ";
        String limitSql = "ORDER BY COUNT(DISTINCT FL.LIKED_BY_USER_ID) DESC " +
                "LIMIT ?";

        if (genreId.isPresent() && year.isPresent()) {
            sql = FIND_ALL + yearSql + groupBySql + genreIdSql + limitSql;
            return jdbcTemplate.query(sql, (rs, rowNum) -> mapRowToFilm(rs), year.get(), genreId.get(), limit);
        } else if (genreId.isPresent()) {
            sql = FIND_ALL + groupBySql + genreIdSql + limitSql;
            return jdbcTemplate.query(sql, (rs, rowNum) -> mapRowToFilm(rs), genreId.get(), limit);
        } else if (year.isPresent()) {
            sql = FIND_ALL + yearSql + groupBySql + limitSql;
            return jdbcTemplate.query(sql, (rs, rowNum) -> mapRowToFilm(rs), year.get(), limit);
        }
        sql = FIND_ALL + groupBySql + limitSql;
        return jdbcTemplate.query(sql, (rs, rowNum) -> mapRowToFilm(rs), limit);
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
        if (matrix == null) {
            return Collections.emptyList();
        }
        Recommender recommender = new Recommender(matrix, true);
        List<Integer> recommendations = recommender.getRecommendations(userId, Optional.empty());
        //second query for recommended films if necessary
        if (!recommendations.isEmpty()) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("(");
            for (Integer filmId : recommendations) {
                stringBuilder.append(filmId).append(", ");
            }
            String sql2 = FIND_ALL +
                    "WHERE FILMS.ID IN " +
                    stringBuilder.substring(0, stringBuilder.length() - 2) + ") GROUP BY FILMS.ID";
            return jdbcTemplate.query(sql2, (rs, rowNum) -> mapRowToFilm(rs));
        } else {
            return Collections.emptyList();
        }
    }

    private Matrix makeMatrixForRecommendations(ResultSet rs) throws SQLException {
        Matrix data = new Matrix();
        try {
            SqlArrayConverter converter = new SqlArrayConverter();
            while (rs.next()) {
                List<Integer> userIds = converter.convert(rs.getArray("user_ids"));
                List<Integer> rates = converter.convert(rs.getArray("rates"));
                for (int i = 0; i < userIds.size(); i++) {
                    data.writeValue(rs.getInt("film_ids"), userIds.get(i), Optional.of(rates.get(i).doubleValue()));
                }
            }
            return data;
        } catch (EmptyResultDataAccessException exp) {
            return null;
        }
    }

    @Override
    public Collection<Film> findCommonFilms(int userId, int friendId) {
        String sql = FIND_ALL +
                "GROUP BY FILMS.ID " +
                "HAVING ARRAY_CONTAINS(LIKES, ?) AND ARRAY_CONTAINS(LIKES, ?) " +
                "ORDER BY LIKES DESC";
        return jdbcTemplate.query(sql, (rs, rowNum) -> mapRowToFilm(rs), userId, friendId);
    }

    @Override
    public Collection<Film> getFilmsByDirector(int directorId, SortBy sortBy) {
        String sql = FIND_ALL +
                "WHERE DIRECTOR_ID = ? " +
                "GROUP BY FILMS.ID ";
        String sqlSortBy = "ORDER BY LIKES DESC";
        if (sortBy == SortBy.year) {
            sqlSortBy = "ORDER BY YEAR(RELEASE_DATE)";
        }
        return jdbcTemplate.query(sql + sqlSortBy, (rs, rowNum) -> mapRowToFilm(rs), directorId);
    }

    private void saveDirectors(Film film) {
        jdbcTemplate.update("DELETE FROM FILM_DIRECTOR WHERE FILM_ID = ?", film.getId());
        List<Director> directors = new ArrayList<>(film.getDirectors());
        int filmId = film.getId();
        String sql = "INSERT INTO FILM_DIRECTOR(DIRECTOR_ID, FILM_ID) VALUES (?,?) ";

        if (!directors.isEmpty()) {
            jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    ps.setInt(1, directors.get(i).getId());
                    ps.setInt(2, filmId);
                }

                @Override
                public int getBatchSize() {
                    return directors.size();
                }
            });
        }
    }

    @Override
    public Collection<Film> searchForFilmsByTitle(String query) {
        String sql = FIND_ALL +
                "WHERE FILMS.NAME ILIKE ? " +
                "GROUP BY FILMS.ID " +
                "ORDER BY FILMS.ID DESC";
        return jdbcTemplate.query(sql, (rs, rowNum) -> mapRowToFilm(rs), "%" + query + "%");
    }

    @Override
    public Collection<Film> searchForFilmsByDirector(String query) {
        String sql = FIND_ALL +
                "WHERE D.NAME ILIKE ? " +
                "GROUP BY FILMS.ID " +
                "ORDER BY FILMS.ID DESC";
        return jdbcTemplate.query(sql, (rs, rowNum) -> mapRowToFilm(rs), "%" + query + "%");
    }

    @Override
    public Collection<Film> searchForFilmsByDirectorAndTitle(String query) {
        String sql = FIND_ALL +
                "WHERE D.NAME ILIKE ? OR FILMS.NAME ILIKE ? " +
                "GROUP BY FILMS.ID " +
                "ORDER BY FILMS.ID DESC";
        return jdbcTemplate.query(sql, (rs, rowNum) -> mapRowToFilm(rs), "%" + query + "%", "%" + query + "%");
    }
}
