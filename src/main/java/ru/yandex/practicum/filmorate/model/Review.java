package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Review {
    private int reviewId;

    private String content;

    @JsonProperty("isPositive")
    @NotNull
    private boolean isPositive;

    @NotNull
    private int userId;

    @NotNull
    private int filmId;
    private int useful;
}
