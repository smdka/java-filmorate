package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;

import static java.util.stream.Collectors.toList;

@Repository
public class InMemoryFilmStorage {
    private final Map<Integer, Film> films;

    public InMemoryFilmStorage() {
        this.films = new HashMap<>();
    }

    public Collection<Film> findAll() {
        return films.values();
    }

    public Film save(Film film) {
        films.put(film.getId(), film);
        return film;
    }

    public Optional<Film> update(Film film) {
        return films.replace(film.getId(), film) == null ?
                Optional.empty() :
                Optional.of(film);
    }

    public boolean deleteById(int filmId) {
        return films.remove(filmId) != null;
    }

    public Optional<Film> findById(int id) {
        Film film = films.get(id);
        return film == null ?
                Optional.empty() :
                Optional.of(film);
    }

    public Collection<Film> findTopNMostPopular(int n) {
        return films.values().stream()
                .sorted(Comparator.comparingInt(Film::getLikesCount).reversed())
                .limit(n)
                .collect(toList());
    }

    public boolean addLike(int filmId, int userId) {
        Film film = films.get(filmId);
        if (film == null) {
            return false;
        }
        film.addLikeFromUser(userId);
        return true;
    }

    public boolean deleteLike(int filmId, int userId) {
        Film film = films.get(filmId);
        if (film == null) {
            return false;
        }
        film.deleteLikeFromUser(userId);
        return true;
    }

    public Collection<Film> getRecommendations(int userId) {
        return null;
    }

    @Override
    public Collection<Film> findCommonFilms(int userId, int friendId) {
        return films.values().stream()
                .filter(film -> film.getWhoLikedUserIds().contains(userId) &&
                                film.getWhoLikedUserIds().contains(friendId))
                .sorted(Comparator.comparingInt(Film::getLikesCount).reversed())
                .collect(toList());
    }

    @Override
    public Collection<Film> getFilmsByDirector(int directorId, String sortBy) {
        SortedSet<Director> directors;
        Collection<Film> result;
        switch (sortBy.toLowerCase()) {
            case "year":
                result = new TreeSet<>(Comparator.comparingInt(film -> film.getReleaseDate().getYear()));
                break;
            case "likes":
                result = new TreeSet<>(Comparator.comparingInt(Film::getLikesCount).reversed());
                break;
            default:
                throw new IllegalArgumentException("Некорректный аргумент: " + sortBy);
        }
        for (Film film : films.values()) {
            directors = film.getDirectors();
            if (directors.stream().anyMatch(director -> director.getId() == directorId)) {
                result.add(film);
            }
        }
        return result;
    }
}
