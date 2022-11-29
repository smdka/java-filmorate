package ru.yandex.practicum.filmorate.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DateValidator implements ConstraintValidator<MinDate, LocalDate> {
    private LocalDate annotationValue;
    @Override
    public void initialize(MinDate annotation) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        annotationValue = LocalDate.parse(annotation.date(), formatter);
    }

    @Override
    public boolean isValid(LocalDate date, ConstraintValidatorContext context) {
        if (date == null) {
            return true;
        }
        return date.isAfter(annotationValue) || date.isEqual(annotationValue);
    }
}
