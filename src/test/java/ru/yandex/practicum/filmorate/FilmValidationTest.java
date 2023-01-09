package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.Film;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.time.LocalDate;
import java.time.Month;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FilmValidationTest {
    public static final String VALID_NAME = "Valid Name";
    public static final String VALID_DESCRIPTION = "Valid Description";
    public static final int VALID_DURATION = 120;
    public static final LocalDate VALID_RELEASE_DATE = LocalDate.of(1999, Month.JULY, 7);
    public static final LocalDate CINEMA_BIRTHDAY = LocalDate.of(1895, Month.DECEMBER, 28);
    public static final int MAX_DESCRIPTION_SIZE = 200;
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    private final Film film = new Film();

    @BeforeEach
    public void setUp() {
        setValidFilmAttributes();
    }

    private void setValidFilmAttributes() {
        film.setTitle(VALID_NAME);
        film.setDescription(VALID_DESCRIPTION);
        film.setDuration(VALID_DURATION);
        film.setReleaseDate(VALID_RELEASE_DATE);
    }

    @Test
    public void validFilmShouldPassValidation() {
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertTrue(violations.isEmpty(), violations.toString());
    }

    @Test
    public void invalidNameShouldFailValidation() {
        film.setTitle("");
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty(), violations.toString());
        violations.clear();

        film.setTitle(null);
        violations = validator.validate(film);
        assertFalse(violations.isEmpty(), violations.toString());
        violations.clear();
    }

    @Test
    public void invalidDescriptionShouldFailValidation() {
        film.setDescription("a".repeat(MAX_DESCRIPTION_SIZE + 1));
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty(), violations.toString());
        violations.clear();

        film.setDescription("a".repeat(MAX_DESCRIPTION_SIZE));
        violations = validator.validate(film);
        assertTrue(violations.isEmpty(), violations.toString());
    }

    @Test
    public void invalidReleaseDateShouldFailValidation() {
        film.setReleaseDate(null);
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty(), violations.toString());
        violations.clear();

        film.setReleaseDate(CINEMA_BIRTHDAY.minusDays(1));
        violations = validator.validate(film);
        assertFalse(violations.isEmpty(), violations.toString());
        violations.clear();

        film.setReleaseDate(CINEMA_BIRTHDAY);
        violations = validator.validate(film);
        assertTrue(violations.isEmpty(), violations.toString());
    }

    @Test
    public void invalidDurationShouldFailValidation() {
        film.setDuration(-1);
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty(), violations.toString());
        violations.clear();

        film.setDuration(0);
        violations = validator.validate(film);
        assertFalse(violations.isEmpty(), violations.toString());
    }
}
