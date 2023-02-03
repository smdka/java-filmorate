package ru.yandex.practicum.filmorate.storage.event;

import ru.yandex.practicum.filmorate.model.FeedEvent;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Operation;

import java.util.List;

public interface FeedEventStorage {
    void save(int userId, EventType eventType, Operation operation, int entityId);
    List<FeedEvent> findAllByUserId(int id);
}
