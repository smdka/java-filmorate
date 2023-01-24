package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.director.DirectorDbStorage;

import java.util.Collection;
import java.util.List;

@Service
@Slf4j
public class DirectorService {
    private final DirectorDbStorage directorDbStorage;

    public DirectorService(DirectorDbStorage directorDbStorage) {
        this.directorDbStorage = directorDbStorage;
    }

    public List<Director> getAll() {
        log.debug("Список всех режиссеров успешно отправлен");
        return directorDbStorage.getAll();
    }

    public Director addDirector (Director director) {
        log.debug("Режиссер добавлен");
        return directorDbStorage.addDirector(director);
    }

    public Director findById (int id) {
        log.debug("Режиссер найден");
        return directorDbStorage.findById(id);
    }

    public boolean deleteById (int id) {
        log.debug("Режиссер удален");
        return directorDbStorage.deleteById(id);
    }

    public Director update (Director director) {
        return directorDbStorage.update(director);
    }
}
