package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.utilities.enums.SearchBy;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.utilities.enums.SortBy;
import ru.yandex.practicum.filmorate.validation.Validator;

import java.util.*;
import javax.validation.Valid;

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
    public Collection<Film> getMostPopularFilms(@RequestParam(defaultValue = "10", name = "count", required = false) int limit,
                                                @RequestParam(name = "genreId", required = false) Optional<Integer> genreId,
                                                @RequestParam(name = "year", required = false) Optional<Integer> year) {
        String request =
                "Получен запрос GET /films/popular/count=" +
                limit +
                (genreId.isPresent() ? "&genreId=" + genreId : "") +
                (year.isPresent() ? "&year=" + year : "");

        log.debug(request);
        return filmService.getTopNMostPopular(limit, genreId, year);
    }

    @GetMapping("/common")
    public Collection<Film> getCommonFilms(@RequestParam int userId, @RequestParam int friendId) {
        log.debug("Получен запрос GET /films/common?userId={}&friendId={}", userId, friendId);
        return filmService.getCommonFilms(userId, friendId);
    }

    @GetMapping("/director/{directorId}")
    public Collection<Film> getFilmsByDirector(@PathVariable int directorId, @RequestParam SortBy sortBy) {
        log.debug("получен запрос GET /films/director/{directorId}?sortBy={}", sortBy);
        return filmService.getFilmsByDirector(directorId, sortBy);
    }

    @PostMapping
    public Film add(@Valid @RequestBody Film film, BindingResult bindingResult) {
        log.debug("Получен запрос POST /films");
        Validator.ifHasErrorsThrowValidationException(bindingResult);
        return filmService.add(film);
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film newFilm, BindingResult bindingResult) {
        log.debug("Получен запрос PUT /films");
        Validator.ifHasErrorsThrowValidationException(bindingResult);
        return filmService.update(newFilm);
    }

    @PutMapping("/{filmId}/like/{userId}")
    public void addLikeToFilm(@PathVariable int filmId, @PathVariable int userId) {
        log.debug("Получен запрос PUT /films/{}/like/{}", filmId, userId);
        filmService.addLikeToFilm(filmId, userId);
    }

    @DeleteMapping("/{filmId}/like/{userId}")
    public void deleteLikeFromFilm(@PathVariable int filmId, @PathVariable int userId) {
        log.debug("Получен запрос DELETE /films/{}/like/{}", filmId, userId);
        filmService.deleteLikeFromFilm(filmId, userId);
    }

    @GetMapping("/search")
    public Collection<Film> searchFilm(@RequestParam String query, @RequestParam Set<SearchBy> by) {
        log.debug("Получен запрос GET /films/search?query={}&by={}", query, by);
        return filmService.searchFilm(query, by);
    }


    @DeleteMapping("/{filmId}")
    public void delete(@PathVariable int filmId) {
        log.debug("Получен запрос DELETE /films/{}", filmId);
        filmService.deleteFilmById(filmId);
    }
}
