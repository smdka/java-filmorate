package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public interface FilmStorage {
    public Collection<Film> findAll();
    public int add(Film film);
    public boolean update(Film film);
    public boolean delete(int filmId);
    public List<Film> getTopN(int n, Comparator<Film> comparator);
    public Optional<Film> getFilmById(int id);
}
