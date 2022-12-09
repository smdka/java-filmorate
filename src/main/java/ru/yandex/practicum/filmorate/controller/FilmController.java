package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.IncorrectParameterException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import javax.validation.Valid;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    private final FilmService filmService;

    @Autowired
    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @GetMapping
    public Collection<Film> findAll() {
        log.debug("Получен запрос GET /films");
        return filmService.getAllFilms();
    }

    @GetMapping("/{id}")
    public Film getFilm(@PathVariable int id) {
        log.debug("Получен запрос GET /films/" + id);
        checkParameterIsNegative(id, "id");
        return filmService.getFilmById(id);
    }

    @GetMapping("/popular")
    public Collection<Film> getMostPopularFilms(@RequestParam(defaultValue = "10") int count) {
        log.debug("Получен запрос GET /films/popular/" + count);
        checkParameterIsNegative(count, "count");
        return filmService.getTopNFilmsByLikes(count);
    }

    private static void checkParameterIsNegative(int parameter, String parameterName) {
        if (parameter <= 0) {
            log.warn("Переданный параметр отрицателен");
            throw new IncorrectParameterException(parameterName);
        }
    }

    @PostMapping
    public Film addFilm(@Valid @RequestBody Film film) {
        log.debug("Получен запрос POST /films");
        return filmService.add(film);
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film newFilm) {
        log.debug("Получен запрос PUT /films");
        return filmService.updateFilm(newFilm);
    }

    @PutMapping("/{id}/like/{userId}")
    public Film addLikeToFilm(@PathVariable int id, @PathVariable int userId) {
        log.debug("Получен запрос PUT /films/" + id + "/like/" + userId);
        checkParameterIsNegative(id, "id");
        checkParameterIsNegative(userId, "userId");
        return filmService.addLikeToFilm(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public Film deleteLikeFromFilm(@PathVariable int id, @PathVariable int userId) {
        log.debug("Получен запрос DELETE /films/" + id + "/like/" + userId);
        checkParameterIsNegative(id, "id");
        checkParameterIsNegative(userId, "userId");
        return filmService.deleteLikeFromFilm(id, userId);
    }
}
