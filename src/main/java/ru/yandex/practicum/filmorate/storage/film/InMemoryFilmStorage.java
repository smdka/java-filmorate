package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;

@Component
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
}
