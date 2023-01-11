package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;
import java.util.function.Supplier;

import static java.util.stream.Collectors.*;

@Service
@Slf4j
//TODO переделать пробрасывание исключения
public class UserService {
    private final UserStorage storage;

    @Autowired
    public UserService(@Qualifier("userDdStorage") UserStorage storage) {
        this.storage = storage;
    }

    public User add(User user) {
        useLoginIfNameIsEmpty(user);
        User u = storage.save(user);
        log.info("Пользователь {} успешно добавлен и ему присвоен id = {}", u.getName(), u.getId());
        return u;
    }

    public User update(User newUser) {
        useLoginIfNameIsEmpty(newUser);
        int userId = newUser.getId();
        User updatedUser = storage.update(newUser).orElseThrow(userNotFoundException(userId));
        log.debug("Пользователь с id = {} успешно отправлен", userId);
        return updatedUser;
    }

    private void useLoginIfNameIsEmpty(User user) {
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
        User user = storage.findById(userId).orElseThrow(userNotFoundException(userId));
        log.debug("Пользователь с id = {} успешно отправлен", user.getId());
        return user;
    }

    private Supplier<UserNotFoundException> userNotFoundException(int userId) {
        return () -> {
            log.warn("Пользователь с id = {} не существует", userId);
            return new UserNotFoundException(String.format("Пользователь с id = %d не существует", userId));
        };
    }

    private void throwUserNotFoundException(int userId) {
        log.warn("Пользователь с id = {} не существует", userId);
        throw new UserNotFoundException(String.format("Пользователь с id = %d не существует", userId));
    }

    public Collection<User> getAllUsers() {
        log.debug("Список всех пользователей успешно отправлен");
        return storage.findAll();
    }

    public void deleteUserById(int userId) {
        if (!storage.deleteById(userId)) {
            throwUserNotFoundException(userId);
        }
        log.debug("Пользователь с id = {} успешно удален", userId);
    }

    public Map<User, User> sendFriendRequest(int fromUserId, int toUserId) {
        User user = storage.findById(fromUserId).orElseThrow(userNotFoundException(fromUserId));
        User friend = storage.findById(toUserId).orElseThrow(userNotFoundException(fromUserId));

        user.addFriend(friend);
        storage.update(user);
        log.debug("Пользователь с id = " + toUserId + " теперь в списке друзей пользователя с id = " + fromUserId);
        log.debug("Друзья пользователя с id = " + fromUserId + ": " + user.getFriendIds());
        log.debug("Друзья пользователя с id = " + toUserId + ": " + friend.getFriendIds());
        return Map.of(user, friend);
    }

    public User deleteFriend(int userId, int friendId) {
        User user = storage.findById(userId).orElseThrow(userNotFoundException(userId));
        User friend = storage.findById(friendId).orElseThrow(userNotFoundException(userId));

        user.deleteFriend(friend);
        storage.update(user);
        log.debug("Пользователь с id = " + friendId + " удален из друзей пользователя с id = " + userId);
        log.debug("Друзья пользователя с id = " + userId + ": " + user.getFriendIds());
        log.debug("Друзья пользователя с id = " + friendId + ": " + friend.getFriendIds());
        return user;
    }

    public List<User> getCommonFriends(int  userId, int friendId) {
        User user = storage.findById(userId).orElseThrow(userNotFoundException(userId));
        User friend = storage.findById(friendId).orElseThrow(userNotFoundException(userId));

        Set<Integer> userFriends = user.getFriendIds();
        userFriends.retainAll(friend.getFriendIds());
        log.debug("Список общих друзей для пользователя с id = " + userId
                + " и пользователя с id = " + friendId + " отправлен");
        return userFriends.stream()
                .map(storage::findById)
                .flatMap(Optional::stream)
                .collect(toList());
    }

    public List<User> getFriends(int userId) {
        User user = storage.findById(userId).orElseThrow(userNotFoundException(userId));
        log.debug("Список друзей для пользователя с id = " + userId + " отправлен");
        return user.getFriendIds().stream()
                .map(storage::findById)
                .flatMap(Optional::stream)
                .collect(toList());
    }
}

