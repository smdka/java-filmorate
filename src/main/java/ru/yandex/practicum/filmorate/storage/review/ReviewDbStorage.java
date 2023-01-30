package ru.yandex.practicum.filmorate.storage.review;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.ReviewNotFoundException;
import ru.yandex.practicum.filmorate.model.Review;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ReviewDbStorage implements ReviewStorage {
    private static final String INCREASE_USEFUL = "UPDATE REVIEWS " +
                                                  "SET USEFUL = USEFUL + 1 " +
                                                  "WHERE ID = ?";
    private static final String DECREASE_USEFUL = "UPDATE REVIEWS " +
                                                  "SET USEFUL = USEFUL - 1 " +
                                                  "WHERE ID = ?";
    private static final String FIND_ALL = "SELECT * FROM REVIEWS ";
    private static final String ADD_FEED = "INSERT INTO USER_FEEDS " +
            "(USER_ID, TIME_STAMP, EVENT_TYPE, OPERATION, ENTITY_ID) " +
            "VALUES (?, ?, ?, ?, ?)";
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Collection<Review> findAll() {
        return jdbcTemplate.query(FIND_ALL, (resultSet, rowNum) -> mapRowToReview(resultSet));
    }

    @Override
    public Optional<Review> findById(int id) {
        String sql = FIND_ALL +
                    "WHERE ID = ?";
        List<Review> results = jdbcTemplate.query(sql, (resultSet, rowNum) -> mapRowToReview(resultSet), id);
        return results.isEmpty() ?
                Optional.empty() :
                Optional.of(results.get(0));
    }

    private Review mapRowToReview(ResultSet resultSet) throws SQLException {
        int id = resultSet.getInt("ID");
        String content = resultSet.getString("CONTENT");
        boolean isPositive = resultSet.getBoolean("IS_POSITIVE");
        int useful = resultSet.getInt("USEFUL");
        int userId = resultSet.getInt("USER_ID");
        int filmId = resultSet.getInt("FILM_ID");
        return new Review(id, content, isPositive, userId, filmId, useful);
    }

    @Override
    public Review save(Review review) {
        String sql = "INSERT INTO REVIEWS (CONTENT, IS_POSITIVE, USER_ID, FILM_ID) " +
                     "VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sql, new String[] {"ID"});
            stmt.setString(1, review.getContent());
            stmt.setBoolean(2, review.getIsPositive());
            stmt.setInt(3, review.getUserId());
            stmt.setInt(4, review.getFilmId());
            return stmt;
        }, keyHolder);

        int reviewId = keyHolder.getKey().intValue();
        review.setReviewId(reviewId);
        jdbcTemplate.update(ADD_FEED,
                review.getUserId(), Instant.now().toEpochMilli(), "REVIEW", "ADD", review.getReviewId());
        return review;
    }

    @Override
    public Optional<Review> update(Review review) {
        String sql = "UPDATE REVIEWS SET " +
                     "CONTENT = ?, IS_POSITIVE = ? " +
                     "WHERE ID = ?";
        if (jdbcTemplate.update(sql, review.getContent(),
                review.getIsPositive(),
                review.getReviewId()) == 0) {
            return Optional.empty();
        }
        sql = FIND_ALL +
             "WHERE ID = ?";
        Review updatedReview = jdbcTemplate.query(sql, (resultSet, rowNum) -> mapRowToReview(resultSet), review.getReviewId()).get(0);
        jdbcTemplate.update(ADD_FEED,
                updatedReview.getReviewId(), Instant.now().toEpochMilli(), "REVIEW", "UPDATE", updatedReview.getFilmId());
        return Optional.of(updatedReview);
    }

    @Override
    public boolean deleteById(int id) {
        Integer filmId = jdbcTemplate.queryForObject("SELECT FILM_ID FROM REVIEWS WHERE ID = ?", Integer.class, id);
        if (filmId != null && jdbcTemplate.update("DELETE FROM REVIEWS WHERE ID = ?", id) > 0) {
            jdbcTemplate.update(ADD_FEED,
                    id, Instant.now().toEpochMilli(), "REVIEW", "REMOVE", filmId);
            return true;
        }
        return false;
    }

    @Override
    public boolean addLike(int reviewId, int userId) {
        String sql = "MERGE INTO REVIEW_LIKES(REVIEW_ID, USER_ID) VALUES (?, ?)";
        if (jdbcTemplate.update(sql, reviewId, userId) > 0) {
            sql = INCREASE_USEFUL;
            jdbcTemplate.update(sql, reviewId);
            return true;
        }
        return false;
    }

    @Override
    public boolean addDislike(int reviewId, int userId) {
        String sql = "MERGE INTO REVIEW_DISLIKES(REVIEW_ID, USER_ID) VALUES (?, ?)";
        if (jdbcTemplate.update(sql, reviewId, userId) > 0) {
            sql = DECREASE_USEFUL;
            jdbcTemplate.update(sql, reviewId);
            return true;
        }
        return false;
    }

    @Override
    public boolean deleteLike(int reviewId, int userId) {
        String sql = "DELETE FROM REVIEW_LIKES " +
                     "WHERE REVIEW_ID = ? AND USER_ID = ?";
        if (jdbcTemplate.update(sql, reviewId, userId) > 0) {
            sql = DECREASE_USEFUL;
            jdbcTemplate.update(sql, reviewId);
            return true;
        }
        return false;
    }

    @Override
    public boolean deleteDislike(int reviewId, int userId) {
        String sql = "DELETE FROM REVIEW_DISLIKES " +
                     "WHERE REVIEW_ID = ? AND USER_ID = ?";
        if (jdbcTemplate.update(sql, reviewId, userId) > 0) {
            sql = INCREASE_USEFUL;
            jdbcTemplate.update(sql, reviewId);
            return true;
        }
        return false;
    }

    @Override
    public Collection<Review> findTopNMostUsefulReviewsByFilmId(int filmId, int n) {
        String sql = FIND_ALL +
                    "WHERE FILM_ID = ? " +
                    "ORDER BY USEFUL DESC " +
                    "LIMIT ?";
        return jdbcTemplate.query(sql, (resultSet, rowNum) -> mapRowToReview(resultSet), filmId, n);
    }

    @Override
    public Collection<Review> findTopNMostUsefulReviewsByFilmId(int n) {
        String sql = FIND_ALL +
                    "ORDER BY USEFUL DESC " +
                    "LIMIT ?";
        return jdbcTemplate.query(sql, (resultSet, rowNum) -> mapRowToReview(resultSet), n);
    }
}
