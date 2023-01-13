package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.GenreNotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.GenreDbStorage;

import java.util.Collection;

@Service
@Slf4j
public class GenreService {
    private final GenreDbStorage storage;

    public GenreService(GenreDbStorage storage) {
        this.storage = storage;
    }

    public Collection<Genre> getAllGenres() {
        log.debug("Список всех жанров успешно отправлен");
        return storage.findAll();
    }

    public Genre getGenreById(int id) {
        Genre genre = storage.findById(id)
                .orElseThrow(() -> new GenreNotFoundException(String.format("Жанр с id = %d не существует", id)));
        log.debug("Жанр с id = {} успешно отправлен", id);
        return genre;
    }
}
