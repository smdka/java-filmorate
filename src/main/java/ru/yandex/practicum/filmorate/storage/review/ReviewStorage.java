package ru.yandex.practicum.filmorate.storage.review;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.Collection;
import java.util.Optional;

public interface ReviewStorage {
    Collection<Review> findAll();

    Optional<Review> findById(int id);

    Review save(Review review);

    Optional<Review> update(Review newReview);

    boolean deleteById(int id);

    boolean addLike(int reviewId, int userId);

    boolean addDislike(int reviewId, int userId);

    boolean deleteLike(int reviewId, int userId);

    boolean deleteDislike(int reviewId, int userId);

    Collection<Review> findTopNMostUsefulReviewsByFilmId(int filmId, int n);

    Collection<Review> findTopNMostUsefulReviewsByFilmId(int n);
}
