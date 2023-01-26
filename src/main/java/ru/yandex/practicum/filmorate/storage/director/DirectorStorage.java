package ru.yandex.practicum.filmorate.storage.director;

import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface DirectorStorage {
    Collection<Director> findAll();

    Optional<Director> findById(int id);

    Director save(Director director);

    boolean deleteById(int id);

    Optional<Director> update(Director director);
}
