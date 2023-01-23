package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;

import static java.util.stream.Collectors.*;

@Repository
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

    @Override
    public Collection<User> findFriendsById(int userId) {
        User user = users.get(userId);
        return user == null ?
                null :
                user.getFriendIds().stream()
                        .map(users::get)
                        .collect(toList());
    }

    @Override
    public boolean addFriend(int userId, int friendId) {
        User user = users.get(userId);
        if (user == null) {
            return false;
        }
        user.addFriend(friendId);
        return true;
    }

    @Override
    public boolean removeFriend(int userId, int friendId) {
        User user = users.get(userId);
        if (user == null) {
            return false;
        }
        user.deleteFriend(friendId);
        return true;
    }

    @Override
    public Collection<User> findCommonFriendsByIds(int firstUserId, int secondUserId) {
        User firstUser = users.get(firstUserId);
        User secondUser = users.get(secondUserId);
        if (firstUser == null || secondUser == null) {
            return Collections.emptyList();
        }
        firstUser.getFriendIds().retainAll(secondUser.getFriendIds());
        return firstUser.getFriendIds().stream()
                .map(users::get)
                .collect(toList());
    }
}
