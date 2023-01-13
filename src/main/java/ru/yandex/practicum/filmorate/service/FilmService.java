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

import static java.util.stream.Collectors.*;

@Service
@Slf4j
public class FilmService {
    private static final String FILM_NOT_EXISTS_MSG = "Фильм с id = %d не существует";
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
        int id = newFilm.getId();
        Film updatedFilm = storage.update(newFilm)
                .orElseThrow(() -> new FilmNotFoundException(String.format(FILM_NOT_EXISTS_MSG, id)));
        log.debug("Фильм с id = {} успешно обновлен", id);
        return updatedFilm;
    }

    public Film getFilmById(int id) {
        Film film = getFilmOrElseThrow(id);
        log.debug("Фильм с id = {} успешно отправлен", id);
        return film;
    }

    public Collection<Film> getAllFilms() {
        log.debug("Список всех фильмов успешно отправлен");
        return storage.findAll();
    }

    public void deleteFilmById(int id) {
        if (!storage.deleteById(id)) {
            throw new FilmNotFoundException(String.format(FILM_NOT_EXISTS_MSG, id));
        }
        log.debug("Фильм с id = {} успешно удален", id);
    }

    public Film addLikeToFilm(int filmId, int userId) {
        Film film = getFilmOrElseThrow(filmId);
        film.addLikeFromUser(userId);
        updateFilmAndLog(userId, film);
        return film;
    }

    private void updateFilmAndLog(int userId, Film film) {
        storage.update(film);
        int filmId = film.getId();
        log.debug("Лайк от пользователя с id = {} успешно добавлен в фильм с id = {}", userId, filmId);
        log.debug("Список id пользователей, " +
                  "поставивших лайк фильму с id = " + filmId +": " + film.getWhoLikedUserIds());
    }

    public Film deleteLikeFromFilm(int filmId, int userId) {
        Film film = getFilmOrElseThrow(filmId);
        film.deleteLikeFromUser(userId);
        updateFilmAndLog(userId, film);
        return film;
    }

    private Film getFilmOrElseThrow(int id) {
        return storage.findById(id)
                .orElseThrow(() -> new FilmNotFoundException(String.format(FILM_NOT_EXISTS_MSG, id)));
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
