package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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
    public User save(User user) {
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public Optional<User> update(User user) {
        return users.replace(user.getId(), user) == null ?
                Optional.empty() :
                Optional.of(user);
    }

    @Override
    public boolean deleteById(int id) {
        return users.remove(id) != null;
    }

    @Override
    public Optional<User> findById(int id) {
        User user = users.get(id);
        return user == null ?
                Optional.empty() :
                Optional.of(user);
    }
}
