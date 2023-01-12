package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;

import static java.util.stream.Collectors.*;

@Service
@Slf4j
//TODO переделать пробрасывание исключения
public class FilmService {
    private final FilmStorage storage;

    @Autowired
    public FilmService(@Qualifier("filmDbStorage") FilmStorage storage) {
        this.storage = storage;
    }

    public Film add(Film film) {
        Film f = storage.save(film);
        log.info("Фильм {} успешно добавлен и ему присвоен id = {}", f.getName(), f.getId());
        return f;
    }

    public Film update(Film newFilm) {
        int filmId = newFilm.getId();
        Film updatedFilm = storage.update(newFilm).orElseThrow(filmNotFoundException(filmId));
        log.debug("Фильм с id = {} успешно обновлен", filmId);
        return updatedFilm;
    }

    public Film getFilmById(int filmId) {
        Film film = storage.findById(filmId).orElseThrow(filmNotFoundException(filmId));
        log.debug("Фильм с id = {} успешно отправлен", film.getId());
        return film;
    }

    public Collection<Film> getAllFilms() {
        log.debug("Список всех фильмов успешно отправлен");
        return storage.findAll();
    }

    public void deleteFilmById(int filmId) {
        if (!storage.deleteById(filmId)) {
            log.warn("Фильм с id = {} не существует", filmId);
            throw new FilmNotFoundException(String.format("Фильм с id = %d не существует", filmId));
        }
        log.debug("Фильм с id = {} успешно удален", filmId);
    }

    public Film addLikeToFilm(int filmId, int userId) {
        Film film = storage.findById(filmId).orElseThrow(filmNotFoundException(filmId));
        film.addLikeFromUser(userId);
        updateFilmAndLog(userId, film);
        return film;
    }

    private Supplier<FilmNotFoundException> filmNotFoundException(int filmId) {
        return () -> {
            log.warn("Фильм с id = {} не существует", filmId);
            return new FilmNotFoundException(String.format("Фильм с id = %d не существует", filmId));
        };
    }

    private void updateFilmAndLog(int userId, Film film) {
        storage.update(film);
        int filmId = film.getId();
        log.debug("Лайк от пользователя с id = {} успешно добавлен в фильм с id = {}", userId, filmId);
        log.debug("Список id пользователей, поставивших лайк фильму с id = " + filmId +": " + film.getWhoLikedUserIds());
    }

    public Film deleteLikeFromFilm(int filmId, int userId) {
        Film film = storage.findById(filmId).orElseThrow(filmNotFoundException(filmId));
        film.deleteLikeFromUser(userId);
        updateFilmAndLog(userId, film);
        return film;
    }

    public Collection<Film> getTopNMostPopular(int n) {
        Comparator<Film> byLikes = Comparator.comparingInt(Film::getLikesCount).reversed();
        List<Film> films = storage.findAll().stream()
                        .sorted(byLikes)
                        .limit(n)
                        .collect(toList());
        log.debug("Топ {} фильмов успешно отправлен", n);
        return films;
    }
}
