package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

public class UserDdStorage implements UserStorage {
    @Override
    public Collection<User> findAll() {
        return null;
    }

    @Override
    public void add(User user) {

    }

    @Override
    public boolean update(User user) {
        return false;
    }

    @Override
    public boolean delete(int id) {
        return false;
    }

    @Override
    public User getUserById(int id) {
        return null;
    }
}
