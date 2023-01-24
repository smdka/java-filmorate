package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.DirectorNotFoundException;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
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
        Director director = directorDbStorage.findById(id)
                .orElseThrow(() ->
                        new DirectorNotFoundException(String.format("Режиссер не найден", id)));
        log.debug("Режиссер найден");
        return director;
    }

    public void deleteById (int id) {
        if(!directorDbStorage.deleteById(id)) {
            throw new DirectorNotFoundException("Режиссер не найден");
        }
        log.debug("Режиссер удален");
    }

    public Director update (Director director) {
        Director dir = directorDbStorage.update(director)
                .orElseThrow(() ->
                        new DirectorNotFoundException(String.format("Режиссер не найден", director.getId())));
        return dir;
    }
}
