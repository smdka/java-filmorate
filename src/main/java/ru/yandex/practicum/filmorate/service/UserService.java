package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;

import static java.util.stream.Collectors.*;

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
        useLoginIfNameIsEmpty(user);
        user.setId(++id);
        storage.add(user);
        log.info("Пользователь {} успешно добавлен и ему присвоен id = {}", user.getName(), user.getId());
        return user;
    }

    public User update(User newUser) {
        useLoginIfNameIsEmpty(newUser);
        int userId = newUser.getId();
        if (!storage.update(newUser)) {
            throwUserNotFoundException(userId);
        }
        log.debug("Пользователь с id = {} успешно отправлен", userId);
        return newUser;
    }

    private static void useLoginIfNameIsEmpty(User user) {
        String name = user.getName();
        if (isEmpty(name)) {
            String login = user.getLogin();
            user.setName(login);
        }
    }

    private static boolean isEmpty(String name) {
        return name == null || name.isBlank();
    }

    public User getUserById(int userId) {
        User user = storage.getUserById(userId);
        checkUserIsNull(userId, user);
        log.debug("Пользователь с id = {} успешно отправлен", user.getId());
        return user;
    }

    private static void checkUserIsNull(int userId, User user) {
        if (user == null) {
            throwUserNotFoundException(userId);
        }
    }

    private static void throwUserNotFoundException(int userId) {
        log.warn("Пользователь с id = {} не существует", userId);
        throw new UserNotFoundException(String.format("Пользователь с id = %d не существует", userId));
    }

    public Collection<User> getAllUsers() {
        log.debug("Список всех пользователей успешно отправлен");
        return storage.findAll();
    }

    public void deleteUserById(int userId) {
        if (!storage.delete(userId)) {
            throwUserNotFoundException(userId);
        }
        log.debug("Пользователь с id = {} успешно удален", userId);
    }

    public Map<User, User> addFriendsToEachOther(int userId, int friendId) {
        User user = storage.getUserById(userId);
        checkUserIsNull(userId, user);
        User friend = storage.getUserById(friendId);
        checkUserIsNull(friendId, friend);

        user.addFriend(friend);
        storage.update(user);
        friend.addFriend(user);
        storage.update(friend);
        log.debug("Пользователи с id = " + userId + " и " + friendId + " теперь друзья");
        log.debug("Друзья пользователя с id = " + userId + ": " + user.getFriendIds());
        log.debug("Друзья пользователя с id = " + friendId + ": " + friend.getFriendIds());
        return Map.of(user, friend);
    }

    public User deleteFriend(int userId, int friendId) {
        User user = storage.getUserById(userId);
        checkUserIsNull(userId, user);
        User friend = storage.getUserById(friendId);
        checkUserIsNull(friendId, friend);

        user.deleteFriend(friend);
        storage.update(user);
        log.debug("Пользователь с id = " + friendId + " удален из друзей пользователя с id = " + userId);
        log.debug("Друзья пользователя с id = " + userId + ": " + user.getFriendIds());
        log.debug("Друзья пользователя с id = " + friendId + ": " + friend.getFriendIds());
        return user;
    }

    public List<User> getCommonFriends(int  userId, int friendId) {
        User user = storage.getUserById(userId);
        checkUserIsNull(userId, user);

        User friend = storage.getUserById(friendId);
        checkUserIsNull(friendId, friend);

        Set<Integer> userFriends = user.getFriendIds();
        userFriends.retainAll(friend.getFriendIds());
        log.debug("Список общих друзей для пользователя с id = " + userId
                + " и пользователя с id = " + friendId + " отправлен");
        return userFriends.stream()
                .map(storage::getUserById)
                .collect(toList());
    }

    public List<User> getFriends(int userId) {
        User user = storage.getUserById(userId);
        checkUserIsNull(userId, user);
        log.debug("Список друзей для пользователя с id = " + userId + " отправлен");
        return user.getFriendIds().stream()
                .map(storage::getUserById)
                .collect(toList());
    }


}

