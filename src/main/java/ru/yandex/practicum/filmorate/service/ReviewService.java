package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exception.ReviewNotFoundException;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Operation;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.event.FeedEventStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.review.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;

@Slf4j
@Service
public class ReviewService {
    private static final String REVIEW_NOT_EXISTS_MSG = "Отзыв с id = %d не существует";
    private static final String USER_NOT_EXISTS_MSG = "Пользователь с id = %d не существует";
    private static final String FILM_NOT_EXISTS_MSG = "Фильм с id = %d не существует";

    private final ReviewStorage reviewStorage;
    private final UserStorage userStorage;
    private final FilmStorage filmStorage;
    private final FeedEventStorage feedEventStorage;

    public ReviewService(ReviewStorage reviewStorage,
                         @Qualifier("userDbStorage") UserStorage userStorage,
                         @Qualifier("filmDbStorage") FilmStorage filmStorage,
                         FeedEventStorage feedEventStorage)
    {
        this.reviewStorage = reviewStorage;
        this.userStorage = userStorage;
        this.filmStorage = filmStorage;
        this.feedEventStorage = feedEventStorage;
    }

    public Review getReviewById(int id) {
        Review review = reviewStorage.findById(id)
                .orElseThrow(() ->
                        new ReviewNotFoundException(String.format(REVIEW_NOT_EXISTS_MSG, id)));
        log.debug("Отзыв с id = {} успешно отправлен", id);
        return review;
    }


    public Review add(Review review) {
        ifUserNotExistsThrowNotFoundException(review.getUserId());

        ifFilmNotExistsThrow(review.getFilmId());

        Review savedReview = reviewStorage.save(review);
        int reviewId = savedReview.getReviewId();

        log.info("Отзыв о фильме с id = {} успешно добавлен и ему присвоен id = {}",
                savedReview.getFilmId(), reviewId);
        feedEventStorage.save(review.getUserId(), EventType.REVIEW, Operation.ADD, reviewId);
        return savedReview;
    }

    private void ifFilmNotExistsThrow(int filmId) {
        if (filmStorage.findById(filmId).isEmpty()) {
            throw new FilmNotFoundException(String.format(FILM_NOT_EXISTS_MSG, filmId));
        }
    }

    private void ifUserNotExistsThrowNotFoundException(int userId) {
        if (userStorage.findById(userId).isEmpty()) {
            throw new UserNotFoundException(String.format(USER_NOT_EXISTS_MSG, userId));
        }
    }

    public Review update(Review newReview) {
        int id = newReview.getReviewId();
        Review updatedReview = reviewStorage.update(newReview)
                .orElseThrow(() ->
                        new ReviewNotFoundException(String.format(REVIEW_NOT_EXISTS_MSG, id)));
        log.debug("Отзыв с id = {} успешно обновлен", id);
        feedEventStorage.save(updatedReview.getReviewId(), EventType.REVIEW, Operation.UPDATE,
                updatedReview.getFilmId());
        return updatedReview;
    }


    public void deleteReviewById(int id) {
        Review review = reviewStorage.findById(id)
                .orElseThrow(() ->
                        new ReviewNotFoundException(String.format(REVIEW_NOT_EXISTS_MSG, id)));
        reviewStorage.deleteById(id);
        log.debug("Отзыв с id = {} успешно удален", id);
        feedEventStorage.save(id, EventType.REVIEW, Operation.REMOVE, review.getFilmId());
    }

    public Collection<Review> getReviewsByFilmId(int filmId, int n) {
        if (filmId > 0) {
            log.debug("Топ-{} отзывов фильма с id = {} успешно отправлен", n, filmId);
            return reviewStorage.findTopNMostUsefulReviewsByFilmId(filmId, n);
        } else if (filmId == 0){
            log.debug("Топ-{} отзывов успешно отправлен", n);
            return reviewStorage.findTopNMostUsefulReviewsByFilmId(n);
        }
        throw new FilmNotFoundException(String.format(FILM_NOT_EXISTS_MSG, filmId));
    }

    public void addLikeToReview(int reviewId, int userId) {
        ifUserNotExistsThrowNotFoundException(userId);
        if (reviewStorage.addLike(reviewId, userId)) {
            log.debug("Лайк от пользователя с id = {} успешно добавлен в отзыв с id = {}", userId, reviewId);
            return;
        }
        log.debug("Не удалось добавить лайк от пользователя с id = {} в отзыв с id = {}", userId, reviewId);
    }

    public void addDislikeToReview(int reviewId, int userId) {
        ifUserNotExistsThrowNotFoundException(userId);
        if (reviewStorage.addDislike(reviewId, userId)) {
            log.debug("Дизлайк от пользователя с id = {} успешно добавлен в отзыв с id = {}", userId, reviewId);
            return;
        }
        log.debug("Не удалось добавить дизлайк от пользователя с id = {} в отзыв с id = {}", userId, reviewId);
    }


    public void deleteLikeFromReview(int reviewId, int userId) {
        ifUserNotExistsThrowNotFoundException(userId);
        if (reviewStorage.deleteLike(reviewId, userId)) {
            log.debug("Лайк от пользователя с id = {} успешно удален из отзыва с id = {}", userId, reviewId);
            return;
        }
        log.debug("Не удалось удалить лайк от пользователя с id = {} в отзыв с id = {}", userId, reviewId);
    }

    public void deleteDislikeFromReview(int reviewId, int userId) {
        ifUserNotExistsThrowNotFoundException(userId);
        if (reviewStorage.deleteDislike(reviewId, userId)) {
            log.debug("Дизлайк от пользователя с id = {} успешно удален из отзыва с id = {}", userId, reviewId);
            return;
        }
        log.debug("Не удалось удалить дизлайк от пользователя с id = {} в отзыв с id = {}", userId, reviewId);
    }
}
