package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;

import static java.util.stream.Collectors.*;

@Repository
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Integer, Film> films;

    public InMemoryFilmStorage() {
        this.films = new HashMap<>();
    }

    @Override
    public Collection<Film> findAll() {
        return films.values();
    }

    @Override
    public Film save(Film film) {
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public Optional<Film> update(Film film) {
        return films.replace(film.getId(), film) == null ?
                Optional.empty() :
                Optional.of(film);
    }

    @Override
    public boolean deleteById(int filmId) {
        return films.remove(filmId) != null;
    }

    @Override
    public Optional<Film> findById(int id) {
        Film film = films.get(id);
        return film == null ?
                Optional.empty() :
                Optional.of(film);
    }

    @Override
    public Collection<Film> findTopNMostPopular(int n) {
        Comparator<Film> byLikesDesc = Comparator.comparingInt(Film::getId).reversed();
        return films.values().stream()
                .sorted(byLikesDesc)
                .limit(n)
                .collect(toList());
    }

    @Override
    public boolean addLike(int filmId, int userId) {
        Film film = films.get(filmId);
        if (film == null) {
            return false;
        }
        film.addLikeFromUser(userId);
        return true;
    }

    @Override
    public boolean deleteLike(int filmId, int userId) {
        Film film = films.get(filmId);
        if (film == null) {
            return false;
        }
        film.deleteLikeFromUser(userId);
        return true;
    }

    @Override
    public Collection<Film> getRecommendations(int userId) {
        return null;
    }
}
