package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.filmorate.validation.MinDate;

import javax.validation.constraints.*;
import java.time.LocalDate;
import java.util.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Film {
    public static final int MAX_DESCRIPTION_SIZE = 200;
    public static final String CINEMA_BIRTHDAY = "28.12.1895";
    private int id;
    private Set<Integer> whoLikedUserIds = new HashSet<>();

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
    private Mpa mpa;
    private Set<Genre> genres = new TreeSet<>();

    public void addLikeFromUser(int userId) {
        whoLikedUserIds.add(userId);
    }

    public void deleteLikeFromUser(int userId) {
        whoLikedUserIds.remove(userId);
    }

    public int getLikesCount() {
        return whoLikedUserIds.size();
    }

    public Set<Genre> getGenres() {
        return new TreeSet<>(genres);
    }

    public Set<Integer> getWhoLikedUserIds() {
        return new HashSet<>(whoLikedUserIds);
    }
}
