package ru.yandex.practicum.filmorate.annotation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = DateValidator.class)

public @interface NotBeforeCinemaBirthday {
    public String message() default "Дата не должна быть раньше 28.12.1895 г.";
    public Class<?>[] groups() default {};
    public Class<? extends Payload>[] payload() default {};
}
