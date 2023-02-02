package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.DirectorNotFoundException;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.utilities.enums.SearchBy;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.Optional;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class FilmService {
    private static final String FILM_NOT_EXISTS_MSG = "Фильм с id = %d не существует";
    private static final String USER_NOT_EXISTS_MSG = "Пользователь с id = %d не существует";
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final DirectorStorage directorStorage;

    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage,
                       @Qualifier("userDbStorage") UserStorage userStorage,
                       DirectorStorage directorStorage) {
        this.filmStorage = filmStorage;
        this.userStorage =  userStorage;
        this.directorStorage = directorStorage;
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

    public Collection<Film> getFilmsByDirector (int directorId, String sortBy) {
            directorStorage.findById(directorId)
                .orElseThrow(() ->
                        new DirectorNotFoundException(String.format("Режиссер %d не найден", directorId)));
            Collection<Film> filmList = filmStorage.getFilmsByDirector(directorId, sortBy);
            log.debug("Список всех фильмов режиссера {} успешно отправлен", directorId);
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

    public Collection<Film> getTopNMostPopular(int limit, Optional<Integer> genreId, Optional<Integer> year) {
        log.debug("Топ {} фильмов успешно отправлен", limit);
        return filmStorage.findTopNMostPopular(limit, genreId, year);
    }

    public List<Film> searchFilm(String query, Set<SearchBy> by) {
       if (by.size() == SearchBy.values().length){
           log.debug("Поиск '{}' по названиям фильмов и режиссерам", query);
           return filmStorage.searchForFilmsByDirectorAndTitle (query);
       }else if (by.contains(SearchBy.director)){
           log.debug("Поиск '{}' по режиссерам", query);
           return filmStorage.searchForFilmsByDirector (query);
       }
        log.debug("Поиск '{}' по названиям", query);
        return filmStorage.searchForFilmsByTitle (query);
    }

    public Collection<Film> getCommonFilms(int userId, int friendId) {
        if (userStorage.findById(userId).isEmpty()) {
            throw new UserNotFoundException(String.format(USER_NOT_EXISTS_MSG, userId));
        } else if (userStorage.findById(friendId).isEmpty()) {
            throw new UserNotFoundException(String.format(USER_NOT_EXISTS_MSG, friendId));
        }
        log.debug("Список общих фильмов пользователя с id = {} и его друга с id = {} отправлен", userId, friendId);
        return filmStorage.findCommonFilms(userId, friendId);
    }

}
