package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;

import static java.util.stream.Collectors.*;

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
    public void add(Film film) {
        films.put(film.getId(), film);
    }

    @Override
    public boolean update(Film film) {
        return films.replace(film.getId(), film) != null;
    }

    @Override
    public boolean delete(int filmId) {
        return films.remove(filmId) != null;
    }

    public List<Film> getTopN(int n, Comparator<Film> comparator) {
        return films.values().stream()
                .sorted(comparator)
                .limit(n)
                .collect(toList());
    }

    @Override
    public Film getFilmById(int id) {
        return films.get(id);
    }
}
