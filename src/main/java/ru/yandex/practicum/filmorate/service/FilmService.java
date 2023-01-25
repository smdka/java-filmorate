package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.DirectorNotFoundException;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.Collection;
import java.util.List;

@Service
@Slf4j
public class FilmService {
    private static final String FILM_NOT_EXISTS_MSG = "Фильм с id = %d не существует";
    private final FilmStorage filmStorage;

    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage) {
        this.filmStorage = filmStorage;
    }

    public Film add(Film film) {
        Film savedFilm = filmStorage.save(film);
        log.info("Фильм {} успешно добавлен и ему присвоен id = {}", savedFilm.getName(), savedFilm.getId());
        return savedFilm;
    }

    public Film update(Film newFilm) {
        int id = newFilm.getId();
        Film updatedFilm = filmStorage.update(newFilm)
                .orElseThrow(() ->
                        new FilmNotFoundException(String.format(FILM_NOT_EXISTS_MSG, id)));
        log.debug("Фильм с id = {} успешно обновлен", id);
        return updatedFilm;
    }

    public Film getFilmById(int id) {
        Film film = filmStorage.findById(id)
                .orElseThrow(() ->
                        new FilmNotFoundException(String.format(FILM_NOT_EXISTS_MSG, id)));
        log.debug("Фильм с id = {} успешно отправлен", id);
        return film;
    }

    public List<Film> getFilmsByDirector (int directorId, String sortBy) {
        List<Film> filmList = filmStorage.getFilmsByDirector(directorId, sortBy).orElseThrow(() ->
                new DirectorNotFoundException(String.format("Режиссер не найден", directorId)));
        return filmList;
    }

    public Collection<Film> getAllFilms() {
        log.debug("Список всех фильмов успешно отправлен");
        return filmStorage.findAll();
    }

    public void deleteFilmById(int id) {
        if (!filmStorage.deleteById(id)) {
            throw new FilmNotFoundException(String.format(FILM_NOT_EXISTS_MSG, id));
        }
        log.debug("Фильм с id = {} успешно удален", id);
    }

    public void addLikeToFilm(int filmId, int userId) {
        if (filmStorage.addLike(filmId, userId)) {
            log.debug("Лайк от пользователя с id = {} успешно добавлен в фильм с id = {}", userId, filmId);
            return;
        }
        log.debug("Не удалось добавить лайк от пользователя с id = {} в фильм с id = {}", userId, filmId);
    }

    public void deleteLikeFromFilm(int filmId, int userId) {
        if (filmStorage.deleteLike(filmId, userId)) {
            log.debug("Лайк от пользователя с id = {} успешно удален из фильма с id = {}", userId, filmId);
            return;
        }
        log.debug("Не удалось удалить лайк от пользователя с id = {} в фильм с id = {}", userId, filmId);
    }

    public Collection<Film> getTopNMostPopular(int n) {
        log.debug("Топ {} фильмов успешно отправлен", n);
        return filmStorage.findTopNMostPopular(n);
    }
}
