package ru.yandex.practicum.filmorate.annotation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = LoginValidator.class)

public @interface NoWhitespaces {
    public String message() default "Строка не должна содержать пробелов";
    public Class<?>[] groups() default {};
    public Class<? extends Payload>[] payload() default {};
}
