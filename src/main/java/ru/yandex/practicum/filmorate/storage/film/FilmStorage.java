package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.utilities.enums.SortBy;

import java.util.Collection;
import java.util.Optional;

public interface FilmStorage {
    Collection<Film> findAll();
    Film save(Film film);
    Optional<Film> update(Film film);
    boolean deleteById(int filmId);
    Optional<Film> findById(int id);
    Collection<Film> findTopNMostPopular(int limit, Optional<Integer> genreId, Optional<Integer> year);
    boolean addLike(int filmId, int userId);
    boolean deleteLike(int filmId, int userId);
    Collection<Film> getRecommendations(int userId);
    Collection<Film> findCommonFilms(int userId, int friendId);
    Collection<Film> getFilmsByDirector (int directorId, SortBy sortBy);
    Collection<Film> searchForFilmsByTitle(String query);
    Collection<Film> searchForFilmsByDirector(String query);
    Collection<Film> searchForFilmsByDirectorAndTitle(String query);
}
