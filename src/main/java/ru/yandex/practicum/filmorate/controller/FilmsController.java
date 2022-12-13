package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import javax.validation.Valid;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmsController {
    private final FilmService filmService;

    @Autowired
    public FilmsController(FilmService filmService) {
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
        return filmService.getFilmById(id);
    }

    @GetMapping("/popular")
    public Collection<Film> getMostPopularFilms(@RequestParam(defaultValue = "10") int count) {
        log.debug("Получен запрос GET /films/popular/" + count);
        return filmService.getTopNFilmsByLikes(count);
    }

    @PostMapping
    public Film add(@Valid @RequestBody Film film, BindingResult bindingResult) {
        log.debug("Получен запрос POST /films");
        checkForErrors(bindingResult);
        return filmService.add(film);
    }

    private static void checkForErrors(BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            for (FieldError e : bindingResult.getFieldErrors()) {
                log.warn("Не пройдена валидация фильма: " + e.getField() + " = " + e.getRejectedValue());
            }
            throw new ValidationException(bindingResult.getFieldErrors().toString());
        }
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film newFilm, BindingResult bindingResult) {
        log.debug("Получен запрос PUT /films");
        checkForErrors(bindingResult);
        return filmService.update(newFilm);
    }

    @PutMapping("/{id}/like/{userId}")
    public Film addLikeToFilm(@PathVariable int id, @PathVariable int userId) {
        log.debug("Получен запрос PUT /films/" + id + "/like/" + userId);
        checkUserIdIsNegative(userId);
        return filmService.addLikeToFilm(id, userId);
    }

    static void checkUserIdIsNegative(int userId) {
        if (userId <= 0) {
            log.warn("Пользователь с id = {} не существует", userId);
            throw new UserNotFoundException("Пользователь с id = " + userId + " не существует");
        }
    }

    @DeleteMapping("/{id}/like/{userId}")
    public Film deleteLikeFromFilm(@PathVariable int id, @PathVariable int userId) {
        log.debug("Получен запрос DELETE /films/" + id + "/like/" + userId);
        checkUserIdIsNegative(userId);
        return filmService.deleteLikeFromFilm(id, userId);
    }
}
