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
import java.util.Collection;

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
        log.debug("Получен запрос GET /films/{}", id);
        return filmService.getFilmById(id);
    }

    @GetMapping("/popular")
    public Collection<Film> getMostPopularFilms(@RequestParam(defaultValue = "10") int count) {
        log.debug("Получен запрос GET /films/popular/{}", count);
        return filmService.getTopNMostPopular(count);
    }

    @PostMapping
    public Film add(@Valid @RequestBody Film film, BindingResult bindingResult) {
        log.debug("Получен запрос POST /films");
        ifHasErrorsThrow(bindingResult);
        return filmService.add(film);
    }

    private void ifHasErrorsThrow(BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            for (FieldError e : bindingResult.getFieldErrors()) {
                log.warn("Не пройдена валидация фильма: {} = {}", e.getField(), e.getRejectedValue());
            }
            throw new ValidationException(bindingResult.getFieldErrors().toString());
        }
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film newFilm, BindingResult bindingResult) {
        log.debug("Получен запрос PUT /films");
        ifHasErrorsThrow(bindingResult);
        return filmService.update(newFilm);
    }

    @PutMapping("/{filmId}/like/{userId}")
    public void addLikeToFilm(@PathVariable int filmId, @PathVariable int userId) {
        log.debug("Получен запрос PUT /films/{}/like/{}", filmId, userId);
        ifNegativeThrow(userId);
        filmService.addLikeToFilm(filmId, userId);
    }

    private void ifNegativeThrow(int userId) {
        if (userId <= 0) {
            throw new UserNotFoundException("Пользователь с id = " + userId + " не существует");
        }
    }

    @DeleteMapping("/{filmId}/like/{userId}")
    public void deleteLikeFromFilm(@PathVariable int filmId, @PathVariable int userId) {
        log.debug("Получен запрос DELETE /films/{}/like/{}", filmId, userId);
        ifNegativeThrow(userId);
        filmService.deleteLikeFromFilm(filmId, userId);
    }

    @DeleteMapping("/{filmId}")
    public void deleteFilm(@PathVariable int filmId) {
        log.debug("Получен запрос DELETE /films/{}", filmId);
        ifNegativeThrow(filmId);
        filmService.deleteFilmById(filmId);
    }
}
