package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public interface FilmStorage {
    public Collection<Film> findAll();
    public void add(Film film);
    public boolean update(Film film);
    public boolean delete(int filmId);
    public List<Film> getTopN(int N, Comparator<Film> comparator);
    public Film getFilmById(int id);
}
