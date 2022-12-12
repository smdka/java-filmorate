package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

public interface UserStorage {
    public Collection<User> findAll();
    public void add(User user);
    public boolean update(User user);
    public boolean delete(int id);
    //TODO переделать на Optional<Film>
    public User getUserById(int id);
}
