package ru.yandex.practicum.filmorate.annotation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class LoginValidator implements ConstraintValidator<NoWhitespaces, String> {
    @Override
    public boolean isValid(String s, ConstraintValidatorContext context) {
        if (s != null) {
            return !s.contains(" ");
        }
        return false;
    }
}
