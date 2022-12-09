package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

public interface UserStorage {
    public Collection<User> findAll();
    public void add(User user);
    public boolean update(User User);
    public boolean delete(int id);
    public User getUserById(int id);
}
