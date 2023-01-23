package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ReviewNotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.review.ReviewStorage;

import java.util.Collection;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {
    private static final String REVIEW_NOT_EXISTS_MSG = "Отзыв с id = %d не существует";
    private final ReviewStorage reviewStorage;
    public Review getReviewById(int id) {
        Review review = reviewStorage.findById(id)
                .orElseThrow(() ->
                        new ReviewNotFoundException(String.format(REVIEW_NOT_EXISTS_MSG, id)));
        log.debug("Отзыв с id = {} успешно отправлен", id);
        return review;
    }


    public Review add(Review review) {
        Review savedReview = reviewStorage.save(review);
        log.info("Отзыв о фильме с id = {} успешно добавлен и ему присвоен id = {}",
                savedReview.getFilmId(), savedReview.getReviewId());
        return savedReview;
    }


    public Review update(Review newReview) {
        int id = newReview.getReviewId();
        Review updatedReview = reviewStorage.update(newReview)
                .orElseThrow(() ->
                        new ReviewNotFoundException(String.format(REVIEW_NOT_EXISTS_MSG, id)));
        log.debug("Отзыв с id = {} успешно обновлен", id);
        return updatedReview;
    }


    public void deleteReviewById(int id) {
        if (!reviewStorage.deleteById(id)) {
            throw new ReviewNotFoundException(String.format(REVIEW_NOT_EXISTS_MSG, id));
        }
        log.debug("Отзыв с id = {} успешно удален", id);
    }

    public Collection<Review> getReviewsByFilmId(int filmId, int n) {
        log.debug("Топ-{} отзывов фильма с id = {} успешно отправлен", n, filmId);
        return reviewStorage.findTopNMostUsefulReviewsByFilmId(filmId, n);
    }

    public void addLikeToReview(int reviewId, int userId) {
        if (reviewStorage.addLike(reviewId, userId)) {
            log.debug("Лайк от пользователя с id = {} успешно добавлен в отзыв с id = {}", userId, reviewId);
            return;
        }
        log.debug("Не удалось добавить лайк от пользователя с id = {} в отзыв с id = {}", userId, reviewId);
    }

    public void addDislikeToReview(int reviewId, int userId) {
        if (reviewStorage.addDislike(reviewId, userId)) {
            log.debug("Дизлайк от пользователя с id = {} успешно добавлен в отзыв с id = {}", userId, reviewId);
            return;
        }
        log.debug("Не удалось добавить дизлайк от пользователя с id = {} в отзыв с id = {}", userId, reviewId);
    }


    public void deleteLikeFromReview(int reviewId, int userId) {
        if (reviewStorage.deleteLike(reviewId, userId)) {
            log.debug("Лайк от пользователя с id = {} успешно удален из отзыва с id = {}", userId, reviewId);
            return;
        }
        log.debug("Не удалось удалить лайк от пользователя с id = {} в отзыв с id = {}", userId, reviewId);
    }

    public void deleteDislikeFromReview(int reviewId, int userId) {
        if (reviewStorage.deleteDislike(reviewId, userId)) {
            log.debug("Дизлайк от пользователя с id = {} успешно удален из отзыва с id = {}", userId, reviewId);
            return;
        }
        log.debug("Не удалось удалить дизлайк от пользователя с id = {} в отзыв с id = {}", userId, reviewId);
    }
}
