package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.ReviewService;
import ru.yandex.practicum.filmorate.validation.Validator;

import javax.validation.Valid;
import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewsController {
    private final ReviewService reviewService;

    @GetMapping("/{id}")
    public Review get(@PathVariable int id) {
        log.debug("Получен запрос GET /reviews/{}", id);
        return reviewService.getReviewById(id);
    }

    @PostMapping
    public Review add(@Valid @RequestBody Review review, BindingResult bindingResult) {
        log.debug("Получен запрос POST /reviews");
        Validator.ifHasErrorsThrowValidationException(bindingResult);
        return reviewService.add(review);
    }

    @PutMapping
    public Review update(@Valid @RequestBody Review newReview, BindingResult bindingResult) {
        log.debug("Получен запрос PUT /reviews");
        Validator.ifHasErrorsThrowValidationException(bindingResult);
        return reviewService.update(newReview);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable int id) {
        log.debug("Получен запрос DELETE /reviews/{}", id);
        reviewService.deleteReviewById(id);
    }

    @GetMapping
    public Collection<Review> getReviewsByFilmId(@RequestParam(defaultValue = "0") int filmId,
                                                 @RequestParam(defaultValue = "10") int count) {
        log.debug("Получен запрос GET /reviews?filmId={}&count={}", filmId, count);
        return reviewService.getReviewsByFilmId(filmId, count);
    }

    @PutMapping("/{reviewId}/like/{userId}")
    public void addLikeToReview(@PathVariable int reviewId, @PathVariable int userId) {
        log.debug("Получен запрос PUT /reviews/{}/like/{}", reviewId, userId);
        reviewService.addLikeToReview(reviewId, userId);
    }

    @PutMapping("/{reviewId}/dislike/{userId}")
    public void addDislikeToReview(@PathVariable int reviewId, @PathVariable int userId) {
        log.debug("Получен запрос PUT /reviews/{}/like/{}", reviewId, userId);
        reviewService.addDislikeToReview(reviewId, userId);
    }

    @DeleteMapping("/{reviewId}/like/{userId}")
    public void deleteLikeFromReview(@PathVariable int reviewId, @PathVariable int userId) {
        log.debug("Получен запрос DELETE /reviews/{}/like/{}", reviewId, userId);
        reviewService.deleteLikeFromReview(reviewId, userId);
    }

    @DeleteMapping("/{reviewId}/dislike/{userId}")
    public void deleteDislikeFromReview(@PathVariable int reviewId, @PathVariable int userId) {
        log.debug("Получен запрос DELETE /reviews/{}/like/{}", reviewId, userId);
        reviewService.deleteDislikeFromReview(reviewId, userId);
    }
}
