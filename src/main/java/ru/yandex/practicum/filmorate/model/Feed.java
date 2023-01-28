package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Feed {
    @NotBlank
    private int eventId;
    @NotBlank
    @Positive
    private int userId;
    @NotBlank
    private long timestamp;
    @NotBlank
    private String eventType;
    @NotBlank
    private String operation;
    @NotBlank
    @Positive
    private int entityId;
}
