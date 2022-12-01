package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<Film> add(@Valid @RequestBody Film film) {
        film.setId(++id);
        films.put(film.getId(), film);
        log.info("Фильм '{}' успешно добавлен и ему присвоен id = {}", film.getName(), film.getId());
        return ResponseEntity.ok(film);
    }

    @PutMapping
    public ResponseEntity<Film> update(@Valid @RequestBody Film newFilm) {
        if (!isExist(newFilm)) {
            log.warn("Фильм с id = " + newFilm.getId() + " не существует");
            return new ResponseEntity<>(newFilm, HttpStatus.NOT_FOUND);
        }
        Film currentFilm = films.get(newFilm.getId());
        currentFilm.updateFrom(newFilm);
        log.info("Фильм '{}' успешно обновлен", currentFilm.getName());
        return ResponseEntity.ok(currentFilm);
    }

    private boolean isExist(Film newFilm) {
        return films.containsKey(newFilm.getId());
    }
}
