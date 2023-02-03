package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FeedEvent {
    private int eventId;

    private int userId;

    private long timestamp;

    @NotBlank
    private String eventType;

    @NotBlank
    private String operation;

    private int entityId;
}
