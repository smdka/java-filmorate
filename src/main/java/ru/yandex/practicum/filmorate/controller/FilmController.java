package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;

import javax.validation.Valid;
import java.util.*;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {
    private final Map<Integer, Film> films = new HashMap<>();
    private int id = 0;

    @GetMapping
    public Collection<Film> findAll() {
        log.info("Список фильмов отправлен");
        return films.values();
    }

    @PostMapping
    public Film add(@Valid @RequestBody Film film) {
        film.setId(++id);
        films.put(film.getId(), film);
        log.info("Фильм '{}' успешно добавлен и ему присвоен id = {}", film.getName(), film.getId());
        return film;
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film newFilm) throws IllegalArgumentException {
        if (!isExist(newFilm)) {
            log.warn("Не удалось добавить фильм");
            throw new IllegalArgumentException("Фильм с id = " + newFilm.getId() + " не существует");
        }
        Film currentFilm = films.get(newFilm.getId());
        currentFilm.updateFrom(newFilm);
        log.info("Фильм '{}' успешно обновлен", currentFilm.getName());
        return currentFilm;
    }

    private boolean isExist(Film newFilm) {
        return films.containsKey(newFilm.getId());
    }
}
