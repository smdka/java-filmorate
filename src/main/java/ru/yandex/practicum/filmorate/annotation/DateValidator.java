package ru.yandex.practicum.filmorate.annotation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.LocalDate;
import java.time.Month;

public class DateValidator implements ConstraintValidator<NotBeforeCinemaBirthday, LocalDate> {
    @Override
    public boolean isValid(LocalDate date, ConstraintValidatorContext context) {
        if (date == null) {
            return true;
        }
        return date.isAfter(LocalDate.of(1895, Month.DECEMBER, 28));
    }
}
