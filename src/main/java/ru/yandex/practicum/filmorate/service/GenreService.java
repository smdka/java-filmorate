package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.GenreNotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.GenreDbStorage;

import java.util.Collection;
import java.util.function.Supplier;

@Service
@Slf4j
public class GenreService {
    private final GenreDbStorage storage;

    public GenreService(GenreDbStorage storage) {
        this.storage = storage;
    }

    public Collection<Genre> getAllGenres() {
        return storage.findAll();
    }

    public Genre getGenreById(int id) {
        Genre genre = storage.findById(id).orElseThrow(genreNotFoundException(id));
        log.debug("Жанр с id = {} успешно отправлен", genre.getId());
        return genre;
    }

    private Supplier<GenreNotFoundException> genreNotFoundException(int id) {
        return () -> {
            log.warn("Жанр с id = {} не существует", id);
            return new GenreNotFoundException(String.format("Жанр с id = %d не существует", id));
        };
    }
}
