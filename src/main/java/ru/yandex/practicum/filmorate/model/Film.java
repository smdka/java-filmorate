package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import ru.yandex.practicum.filmorate.annotation.NotBeforeCinemaBirthday;

import javax.validation.constraints.*;
import java.time.LocalDate;

@Data
public class Film {
    public static final int MAX_DESCRIPTION_SIZE = 200;
    private int id;
    @NotBlank(message = "Имя фильма обязательно")
    private String name;
    @Size(max = MAX_DESCRIPTION_SIZE, message = "Описание фильма не может быть больше 200 символов")
    private String description;
    @NotNull(message = "Дата релиза обязательна")
    @NotBeforeCinemaBirthday
    private LocalDate releaseDate;
    @Positive(message = "Продолжительность фильма должна быть больше 0")
    private int duration;

    public void updateFrom(Film newFilm) {
        this.name = newFilm.getName();
        this.description = newFilm.getDescription();
        this.releaseDate = newFilm.getReleaseDate();
        this.duration = newFilm.getDuration();
    }
}
