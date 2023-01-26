package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.Optional;

public interface FilmStorage {
    Collection<Film> findAll();
    Film save(Film film);
    Optional<Film> update(Film film);
    boolean deleteById(int filmId);
    Optional<Film> findById(int id);
    Collection<Film> findTopNMostPopular(int n);
    boolean addLike(int filmId, int userId);
    boolean deleteLike(int filmId, int userId);
    Collection<Film> findCommonFilms(int userId, int friendId);
}
