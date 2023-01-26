package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.review.ReviewDbStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Sql({"/drop_schema.sql", "/schema.sql", "/test_data.sql"})
class ReviewDbStorageTest {
    private static final int WRONG_ID = 9999;
    private static final int EXPECTED_REVIEWS_COUNT = 3;
    private final ReviewDbStorage reviewDbStorage;

    @Test
    void findByIdTest() {
        Optional<Review> reviewOptional = reviewDbStorage.findById(1);
        assertThat(reviewOptional)
                .isPresent()
                .hasValueSatisfying(review ->
                        assertThat(review).hasFieldOrPropertyWithValue("reviewId", 1));

        reviewOptional = reviewDbStorage.findById(WRONG_ID);

        assertThat(reviewOptional).isNotPresent();
    }

    @Test
    void findAllTest() {
        List<Review> reviews = new ArrayList<>(reviewDbStorage.findAll());

        assertThat(reviews).hasSize(EXPECTED_REVIEWS_COUNT);
    }

    @Test
    void saveTest() {
        Review review = new Review();
        review.setContent("Review 4");
        review.setUseful(0);
        review.setFilmId(3);
        review.setUserId(2);
        review.setIsPositive(true);

        Review savedReview = reviewDbStorage.save(review);

        assertThat(savedReview)
                .usingRecursiveComparison()
                .ignoringFields("reviewId")
                .isEqualTo(review);

        List<Review> reviews = new ArrayList<>(reviewDbStorage.findAll());

        assertThat(reviews).hasSize(EXPECTED_REVIEWS_COUNT + 1);
    }

    @Test
    void updateTest() {
        Review review = new Review();
        review.setReviewId(1);
        review.setContent("Updated review");
        review.setUseful(1);
        review.setFilmId(1);
        review.setUserId(1);
        review.setIsPositive(true);

        reviewDbStorage.update(review);
        Optional<Review> updatedReview = reviewDbStorage.findById(1);

        assertThat(updatedReview)
                .isPresent()
                .isEqualTo(Optional.of(review));

        review.setReviewId(WRONG_ID);

        updatedReview = reviewDbStorage.update(review);

        assertThat(updatedReview).isNotPresent();
    }

    @Test
    void deleteByIdTest() {
        boolean isDeleted = reviewDbStorage.deleteById(1);

        assertThat(isDeleted).isTrue();

        List<Review> reviews = new ArrayList<>(reviewDbStorage.findAll());

        assertThat(reviews).hasSize(EXPECTED_REVIEWS_COUNT - 1);

        isDeleted = reviewDbStorage.deleteById(WRONG_ID);

        assertThat(isDeleted).isFalse();
    }

    @Test
    void addLikeTest() {
        Review review = reviewDbStorage.findById(1).get();
        int useful = review.getUseful();

        assertThat(reviewDbStorage.addLike(1, 2)).isTrue();

        review = reviewDbStorage.findById(1).get();

        assertEquals(useful + 1, review.getUseful());
    }

    @Test
    void addDislikeTest() {
        Review review = reviewDbStorage.findById(1).get();
        int useful = review.getUseful();

        assertThat(reviewDbStorage.addDislike(1, 3)).isTrue();

        review = reviewDbStorage.findById(1).get();

        assertEquals(useful - 1, review.getUseful());
    }

    @Test
    void deleteLikeTest() {
        reviewDbStorage.addLike(1, 2);
        Review review = reviewDbStorage.findById(1).get();
        int useful = review.getUseful();

        assertThat(reviewDbStorage.deleteLike(1, 2)).isTrue();

        review = reviewDbStorage.findById(1).get();

        assertEquals(useful - 1, review.getUseful());
    }

    @Test
    void deleteDislikeTest() {
        reviewDbStorage.addDislike(1, 2);
        Review review = reviewDbStorage.findById(1).get();
        int useful = review.getUseful();

        assertThat(reviewDbStorage.deleteDislike(1, 2)).isTrue();

        review = reviewDbStorage.findById(1).get();

        assertEquals(useful + 1, review.getUseful());
    }

    @Test
    void findTopNMostUsefulReviewsByFilmIdTest() {
        List<Review> reviews = new ArrayList<>(
                reviewDbStorage.findTopNMostUsefulReviewsByFilmId(EXPECTED_REVIEWS_COUNT));

        assertThat(reviews).hasSize(EXPECTED_REVIEWS_COUNT);
        assertThat(reviews.get(0))
                .hasFieldOrPropertyWithValue("reviewId", 3);
        assertThat(reviews.get(1))
                .hasFieldOrPropertyWithValue("reviewId", 1);
        assertThat(reviews.get(2))
                .hasFieldOrPropertyWithValue("reviewId", 2);

        reviews = new ArrayList<>(reviewDbStorage.findTopNMostUsefulReviewsByFilmId(1, 2));

        assertThat(reviews).hasSize(2);
        assertThat(reviews.get(0))
                .hasFieldOrPropertyWithValue("reviewId", 1);
        assertThat(reviews.get(1))
                .hasFieldOrPropertyWithValue("reviewId", 2);
    }
}