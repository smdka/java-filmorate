package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import java.time.LocalDateTime;
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
    private LocalDateTime timeStamp;
    @NotBlank
    private String eventType;
    @NotBlank
    private String operation;
    @NotBlank
    @Positive
    private int entityId;
}
