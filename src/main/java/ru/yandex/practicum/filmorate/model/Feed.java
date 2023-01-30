package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Feed {

    @NotNull
    private int eventId;

    @NotNull
    @Positive
    private int userId;

    @NotNull
    private long timestamp;

    @NotBlank
    private String eventType;

    @NotBlank
    private String operation;

    @NotNull
    @Positive
    private int entityId;
}
