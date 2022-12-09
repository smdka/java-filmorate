package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import ru.yandex.practicum.filmorate.validation.MinDate;

import javax.validation.constraints.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
public class Film {
    public static final int MAX_DESCRIPTION_SIZE = 200;
    public static final String CINEMA_BIRTHDAY = "28.12.1895";
    private int id;
    private final Set<Integer> likesFromUsers = new HashSet<>();

    @NotBlank(message = "Имя фильма обязательно")
    private String name;

    @Size(max = MAX_DESCRIPTION_SIZE,
            message = "Описание фильма не может быть больше" + MAX_DESCRIPTION_SIZE + "символов")
    private String description;

    @NotNull(message = "Дата релиза обязательна")
    @MinDate(date = CINEMA_BIRTHDAY)
    private LocalDate releaseDate;

    @Positive(message = "Продолжительность фильма должна быть больше 0")
    private int duration;

    public void addLikeFromUser(int userId) {
        likesFromUsers.add(userId);
    }

    public void deleteLikeFromUser(int userId) {
        likesFromUsers.remove(userId);
    }

    public int getLikesCount() {
        return likesFromUsers.size();
    }

    public Set<Integer> getLikesFromUsers() {
        return new HashSet<>(likesFromUsers);
    }
}
