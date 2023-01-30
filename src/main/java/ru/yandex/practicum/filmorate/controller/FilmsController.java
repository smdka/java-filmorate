package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.film.SortBy;

import java.util.*;
import javax.validation.Valid;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

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
        String request = "Получен запрос GET /films/popular/count=" + limit + (genreId.isPresent() ? "&genreId=" + genreId : "") + (year.isPresent() ? "&year=" + year : "");
        log.debug(request);
        return filmService.getTopNMostPopular(limit, genreId, year);
    }

    @GetMapping("/common")
    public Collection<Film> getCommonFilms(@RequestParam int userId, @RequestParam int friendId) {
        log.debug("Получен запрос GET /films/common?userId={}&friendId={}", userId, friendId);
        return filmService.getCommonFilms(userId, friendId);
    }

    @GetMapping("/director/{directorId}")
    public Collection<Film> getFilmsByDirector(@PathVariable int directorId, @RequestParam String sortBy) {
        boolean present = Arrays.stream(SortBy.values()).anyMatch(x -> Objects.equals(x.toString(), sortBy));
        if (!present) {
            throw new RuntimeException("неверный запрос параметра сортировки");
        }
        log.debug("получен запрос GET /films/director/{directorId}?sortBy={}", sortBy);
        return filmService.getFilmsByDirector(directorId, sortBy);
    }

    @PostMapping
    public Film add(@Valid @RequestBody Film film, BindingResult bindingResult) {
        log.debug("Получен запрос POST /films");
        ifHasErrorsThrow(bindingResult);
        return filmService.add(film);
    }

    private void ifHasErrorsThrow(BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
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

    @GetMapping("/search")
    public List<Film> searchFilm(@RequestParam String query, @RequestParam String by) {
        if (query == null || by == null || query.isEmpty() || by.isEmpty()) {
            throw new ValidationException("Не корректный запрос на поиск");
        }
        log.debug("Получен запрос GEt /films/search?query= {} &by=by {}", query, by);
        List<Film> filmsForReturn = filmService.searchFilm(query, by);
        return filmsForReturn;
    }


    @DeleteMapping("/{filmId}")
    public void delete(@PathVariable int filmId) {
        log.debug("Получен запрос DELETE /films/{}", filmId);
        ifNegativeThrow(filmId);
        filmService.deleteFilmById(filmId);
    }
}
