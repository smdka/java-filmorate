package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserService {
    private final UserStorage storage;
    int id;

    @Autowired
    public UserService(UserStorage storage) {
        this.storage = storage;
        id = 0;
    }

    public User add(User user) {
        user.setId(++id);
        storage.add(user);
        log.info("Пользователь '{}' успешно добавлен и ему присвоен id = {}", user.getName(), user.getId());
        return user;
    }

    public User getUserById(int userId) {
        User user = storage.getUserById(userId);
        checkUserIsNull(userId, user);

        log.debug("Пользователь с id = '{}' успешно отправлен", user.getId());
        return user;
    }

    private static void checkUserIsNull(int userId, User user) {
        if (user == null) {
            userIdIsInvalid(userId);
        }
    }

    private static void userIdIsInvalid(int userId) {
        log.warn("Передан неверный id пользователя: {}", userId);
        throw new NoSuchElementException(String.format("Пользователь с id = %d не существует", userId));
    }

    public Collection<User> getAllUsers() {
        log.debug("Список всех пользователей успешно отправлен");
        return storage.findAll();
    }

    public void deleteUserById(int userId) {
        if (!storage.delete(userId)) {
            userIdIsInvalid(userId);
        }
        log.debug("Пользователь с id = '{}' успешно удален", userId);
    }

    public void addFriendsToEachOther(int userId, int friendId) {
        User user = storage.getUserById(userId);
        checkUserIsNull(userId, user);

        User friend = storage.getUserById(friendId);
        checkUserIsNull(friendId, friend);

        user.addFriend(friend);
        storage.update(user);
        friend.addFriend(user);
        storage.update(friend);
    }

    public void deleteFriendFrom(int userId, int friendId) {
        User user = storage.getUserById(userId);
        checkUserIsNull(userId, user);

        User friend = storage.getUserById(friendId);
        checkUserIsNull(friendId, friend);

        user.deleteFriend(friend);
        storage.update(user);
    }

    public List<User> getCommonFriends(int  userId, int friendId) {
        User user = storage.getUserById(userId);
        checkUserIsNull(userId, user);

        User friend = storage.getUserById(friendId);
        checkUserIsNull(friendId, friend);

        Set<Integer> userFriends = user.getFriends();
        userFriends.retainAll(friend.getFriends());
        return userFriends.stream()
                .map(storage::getUserById)
                .collect(Collectors.toList());
    }
}

