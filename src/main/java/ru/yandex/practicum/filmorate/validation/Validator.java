package ru.yandex.practicum.filmorate.validation;

import org.springframework.validation.BindingResult;
import ru.yandex.practicum.filmorate.exception.ValidationException;

public class Validator {
    public static void ifHasErrorsThrowValidationException(BindingResult br) {
        if (br.hasErrors()) {
            throw new ValidationException(br.getFieldErrors().toString());
        }
    }
}
