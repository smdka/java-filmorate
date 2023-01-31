package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.SearchBy;

import java.util.Collection;
import java.util.List;
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
    Collection<Film> getFilmsByDirector (int directorId, String sortBy);
    List<Film> searchForFilmsByTitle(String query);
    List<Film> searchForFilmsByDirector(String query);
    List<Film> searchForFilmsByDirectorAndTitle(String query);
}
