package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.DirectorNotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.director.DirectorDbStorage;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;

import java.util.Collection;

@Service
@Slf4j
public class DirectorService {
    private static final String DIRECTOR_NOT_FOUND_MSG = "Режиссер c id=%d не найден";
    private final DirectorStorage directorDbStorage;

    public DirectorService(DirectorDbStorage directorDbStorage) {
        this.directorDbStorage = directorDbStorage;
    }

    public Collection<Director> getAllDirectors() {
        log.debug("Список всех режиссеров успешно отправлен");
        return directorDbStorage.findAll();
    }

    public Director addDirector (Director director) {
        log.debug("Режиссер {} {} добавлен", director.getId(), director.getName());
        return directorDbStorage.save(director);
    }

    public Director findById (int id) {
        Director director = directorDbStorage.findById(id)
                .orElseThrow(() ->
                        new DirectorNotFoundException(String.format(DIRECTOR_NOT_FOUND_MSG, id)));
        log.debug("Режиссер {} найден", id);
        return director;
    }

    public void deleteById (int id) {
        if(!directorDbStorage.deleteById(id)) {
            throw new DirectorNotFoundException(String.format(DIRECTOR_NOT_FOUND_MSG, id));
        }
        log.debug("Режиссер {} удален", id);
    }

    public Director update (Director director) {
        Director dir = directorDbStorage.update(director)
                .orElseThrow(() ->
                        new DirectorNotFoundException(String.format(DIRECTOR_NOT_FOUND_MSG, director.getId())));
        log.debug("Режиссер {} обновлен", dir.getId());
        return dir;
    }
}
