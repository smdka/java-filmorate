package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Component
public class InMemoryUserStorage implements UserStorage {
    private final Map<Integer, User> users;

    public InMemoryUserStorage() {
        this.users = new HashMap<>();
    }

    @Override
    public Collection<User> findAll() {
        return users.values();
    }

    @Override
    public void add(User user) {
        users.put(user.getId(), user);
    }

    @Override
    public boolean update(User user) {
        return users.replace(user.getId(), user) != null;
    }

    @Override
    public boolean delete(int id) {
        return users.remove(id) != null;
    }

    @Override
    public User getUserById(int id) {
        return users.get(id);
    }
}
