package ru.yandex.practicum.filmorate.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = DateValidator.class)

public @interface MinDate {
    public String date();
    public String message() default "Дата не должна быть раньше указанной";
    public Class<?>[] groups() default {};
    public Class<? extends Payload>[] payload() default {};
}
