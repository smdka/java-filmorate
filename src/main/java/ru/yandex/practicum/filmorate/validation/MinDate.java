package ru.yandex.practicum.filmorate.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = DateValidator.class)

public @interface MinDate {
    String date();
    String message() default "Дата не должна быть раньше указанной";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
