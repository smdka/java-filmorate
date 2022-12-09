package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.Collection;
import java.util.Comparator;
import java.util.NoSuchElementException;

@Service
@Slf4j
public class FilmService {
    private final FilmStorage storage;
    private int id;

    @Autowired
    public FilmService(FilmStorage storage) {
        this.storage = storage;
        this.id = 0;
    }

    public Film add(Film film) {
        film.setId(++id);
        storage.add(film);
        log.info("Фильм '{}' успешно добавлен и ему присвоен id = {}", film.getName(), film.getId());
        return film;
    }

    public Film getFilmById(int filmId) {
        Film film = storage.getFilmById(filmId);
        checkFilmIsNull(filmId, film);

        log.debug("Фильм с id = '{}' успешно отправлен", film.getId());
        return film;
    }

    private static void filmIdIsInvalid(int filmId) {
        log.warn("Передан неверный id фильма: {}", filmId);
        throw new NoSuchElementException(String.format("Фильм с id = %d не существует", filmId));
    }

    public Collection<Film> getAllFilms() {
        log.debug("Список всех фильмов успешно отправлен");
        return storage.findAll();
    }

    public void deleteFilmById(int filmId) {
        if (!storage.delete(filmId)) {
            filmIdIsInvalid(filmId);
        }
        log.debug("Фильм с id = '{}' успешно удален", filmId);
    }

    public Film addLikeToFilm(int filmId, int userId) {
        Film film = storage.getFilmById(filmId);
        checkFilmIsNull(filmId, film);

        film.addLikeFromUser(userId);
        updateFilmAndLog(filmId, userId, film);
        return film;
    }

    private static void checkFilmIsNull(int filmId, Film film) {
        if (film == null) {
            filmIdIsInvalid(filmId);
        }
    }

    private void updateFilmAndLog(int filmId, int userId, Film film) {
        storage.update(film);
        log.debug("Лайк от пользователя с id = {}' успешно добавлен в фильм с id = {}", userId, filmId);
    }

    public Film deleteLikeFromFilm(int filmId, int userId) {
        Film film = storage.getFilmById(filmId);
        checkFilmIsNull(filmId, film);

        film.deleteLikeFromUser(userId);
        updateFilmAndLog(filmId, userId, film);
        return film;
    }

    public Collection<Film> getTopNFilmsByLikes(int n) {
        Comparator<Film> byLikes = Comparator.comparingInt(Film::getLikesCount).reversed();
        log.debug("Топ {} фильмов успешно отправлен", n);
        return storage.getTopN(n, byLikes);
    }

    public Film updateFilm(Film newFilm) {
        if (!storage.update(newFilm)) {
            filmIdIsInvalid(newFilm.getId());
        }
        log.debug("Фильм с id = '{}' успешно обновлен", newFilm.getId());
        return newFilm;
    }
}
