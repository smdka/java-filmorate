package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.utilities.enums.SortBy;

import java.util.*;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toList;

@Repository
public class InMemoryFilmStorage implements FilmStorage {
    private static final Comparator<Film> BY_LIKES_DESC = Comparator.comparingInt(Film::getLikesCount).reversed();
    private static final Comparator<Film> BY_FILM_ID_DESC = Comparator.comparingInt(Film::getId).reversed();
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

    @Override
    public Collection<Film> findTopNMostPopular(int limit, Optional<Integer> genreId, Optional<Integer> year) {
        Predicate<Film> p = film -> true;
        if (genreId.isPresent() && year.isPresent()) {
            p = film -> isReleaseYearEquals(year.get(), film) && hasGenreId(genreId.get(), film);
        } else if (genreId.isPresent()) {
            p = film -> hasGenreId(genreId.get(), film);
        } else if (year.isPresent()) {
            p = film -> isReleaseYearEquals(year.get(), film);
        }
        return films.values().stream()
                .filter(p)
                .sorted(BY_LIKES_DESC)
                .limit(limit)
                .collect(toList());
    }

    private boolean hasGenreId(int genreId, Film film) {
        return film.getGenres().stream().anyMatch(genre -> genre.getId() == genreId);
    }

    private boolean isReleaseYearEquals(int year, Film film) {
        return getYear(film) == year;
    }

    private int getYear(Film film) {
        return film.getReleaseDate().getYear();
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
        return Collections.emptyList();
    }

    public Collection<Film> findCommonFilms(int userId, int friendId) {
        return films.values().stream()
                .filter(film -> film.getWhoLikedUserIds().contains(userId) &&
                                film.getWhoLikedUserIds().contains(friendId))
                .sorted(BY_LIKES_DESC)
                .collect(toList());
    }

    public Collection<Film> getFilmsByDirector(int directorId, SortBy sortBy) {
        Comparator<Film> c = BY_LIKES_DESC;
        if (sortBy == SortBy.year) {
            c = Comparator.comparingInt(this::getYear).reversed();
        }
        return films.values().stream()
                .filter(film -> hasDirectorId(directorId, film))
                .sorted(c)
                .collect(toList());
    }

    private boolean hasDirectorId(int directorId, Film film) {
        return film.getDirectors().stream().anyMatch(director -> director.getId() == directorId);
    }

    @Override
    public Collection<Film> searchForFilmsByTitle(String query) {
        return films.values().stream()
                .filter(film -> isFilmTitleContains(query, film))
                .sorted(BY_FILM_ID_DESC)
                .collect(toList());
    }

    private boolean isFilmTitleContains(String query, Film film) {
        return film.getName().toLowerCase().contains(query.toLowerCase());
    }

    @Override
    public Collection<Film> searchForFilmsByDirector(String query) {
        return films.values().stream()
                .filter(film -> isDirectorNameContains(query, film))
                .sorted(BY_FILM_ID_DESC)
                .collect(toList());
    }

    private boolean isDirectorNameContains(String query, Film film) {
        return film.getDirectors().stream()
                .anyMatch(director -> director.getName().toLowerCase().contains(query.toLowerCase()));
    }

    @Override
    public Collection<Film> searchForFilmsByDirectorAndTitle(String query) {
        return films.values().stream()
                .filter(film -> isFilmTitleContains(query, film) || isDirectorNameContains(query, film))
                .sorted(BY_FILM_ID_DESC)
                .collect(toList());
    }
}
