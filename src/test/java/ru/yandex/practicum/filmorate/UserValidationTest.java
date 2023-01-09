package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.User;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.time.LocalDate;
import java.time.Month;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UserValidationTest {
    public static final String VALID_NAME = "Valid Name";
    public static final String VALID_EMAIL = "pepe_the_frog@yandex.ru";
    public static final String VALID_LOGIN = "Valid_Login";
    public static final LocalDate VALID_BIRTHDAY = LocalDate.of(1989, Month.MAY, 1);
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    private final User user = new User();

    @BeforeEach
    public void setUp() {
        setValidUserAttributes();
    }

    private void setValidUserAttributes() {
        user.setName(VALID_NAME);
        user.setEmail(VALID_EMAIL);
        user.setLogin(VALID_LOGIN);
        user.setBirthday(VALID_BIRTHDAY);
    }

    @Test
    public void validUserShouldPassValidation() {
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertTrue(violations.isEmpty(), violations.toString());
    }

    @Test
    public void invalidEmailShouldFailValidation() {
        user.setEmail(" ");
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty(), violations.toString());
        violations.clear();

        user.setEmail("@mail.ru");
        violations = validator.validate(user);
        assertFalse(violations.isEmpty(), violations.toString());
        violations.clear();

        user.setEmail(null);
        violations = validator.validate(user);
        assertFalse(violations.isEmpty(), violations.toString());
        violations.clear();
    }

    @Test
    public void invalidLoginShouldFailValidation() {
        user.setLogin(" ");
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty(), violations.toString());
        violations.clear();

        user.setLogin("I have whitespaces");
        violations = validator.validate(user);
        assertFalse(violations.isEmpty(), violations.toString());
        violations.clear();

        user.setLogin(null);
        violations = validator.validate(user);
        assertFalse(violations.isEmpty(), violations.toString());
        violations.clear();
    }

    @Test
    public void invalidBirthdayShouldFailValidation() {
        user.setBirthday(LocalDate.now().plusDays(1));
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty(), violations.toString());
        violations.clear();

        user.setBirthday(LocalDate.now());
        violations = validator.validate(user);
        assertTrue(violations.isEmpty(), violations.toString());
    }
}
