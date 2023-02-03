package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.storage.event.FeedEventStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;

@Service
@Slf4j
public class UserService {
    private static final String USER_NOT_EXISTS_MSG = "Пользователь с id = %d не существует";
    private final UserStorage userStorage;
    private final FilmStorage filmStorage;
    private final FeedEventStorage feedEventStorage;

    public UserService(@Qualifier("userDbStorage") UserStorage userStorage,
                       @Qualifier("filmDbStorage") FilmStorage filmStorage,
                       FeedEventStorage feedEventStorage) {
        this.userStorage = userStorage;
        this.filmStorage = filmStorage;
        this.feedEventStorage = feedEventStorage;
    }

    public User add(User user) {
        useLoginIfNameIsEmpty(user);
        User savedUser = userStorage.save(user);
        log.info("Пользователь {} успешно добавлен и ему присвоен id = {}", savedUser.getName(), savedUser.getId());
        return savedUser;
    }

    public User update(User newUser) {
        useLoginIfNameIsEmpty(newUser);
        int id = newUser.getId();
        User updatedUser = userStorage.update(newUser)
                .orElseThrow(() ->
                        new UserNotFoundException(String.format(USER_NOT_EXISTS_MSG, id)));
        log.debug("Пользователь с id = {} успешно отправлен", id);
        return updatedUser;
    }

    private void useLoginIfNameIsEmpty(User user) {
        String name = user.getName();
        if (isEmpty(name)) {
            String login = user.getLogin();
            user.setName(login);
        }
    }

    private boolean isEmpty(String name) {
        return name == null || name.isBlank();
    }

    public User getUserById(int id) {
        User user = userStorage.findById(id)
                .orElseThrow(() ->
                        new UserNotFoundException(String.format(USER_NOT_EXISTS_MSG, id)));
        log.debug("Пользователь с id = {} успешно отправлен", id);
        return user;
    }

    public Collection<User> getAllUsers() {
        log.debug("Список всех пользователей успешно отправлен");
        return userStorage.findAll();
    }

    public void deleteUserById(int id) {
        if (!userStorage.deleteById(id)) {
            throw new UserNotFoundException(String.format(USER_NOT_EXISTS_MSG, id));
        }
        log.debug("Пользователь с id = {} успешно удален", id);
    }

    public void sendFriendRequest(int fromUserId, int toUserId) {
        if (userStorage.addFriend(fromUserId, toUserId)) {
            log.debug("Пользователь с id = {} теперь в списке друзей пользователя с id = {}", toUserId, fromUserId);
            feedEventStorage.save(fromUserId, EventType.FRIEND, Operation.ADD, toUserId);
            return;
        }
        log.debug("Не удалось добавить пользователя с id = {} в список друзей пользователя с id = {}",
                  toUserId, fromUserId);
    }

    public void deleteFriend(int userId, int friendId) {
        if (userStorage.removeFriend(userId, friendId)) {
            log.debug("Пользователь с id = {} удален из друзей пользователя с id = {}", friendId, userId);
            feedEventStorage.save(userId, EventType.FRIEND, Operation.REMOVE, friendId);
            return;
        }
        log.debug("Не удалось удалить пользователя с id = {} из списка друзей пользователя с id = {}",
                friendId, userId);
    }

    public Collection<User> getCommonFriends(int  userId, int friendId) {
        log.debug("Список общих друзей для пользователя с id = {} и пользователя с id = {} отправлен",
                  userId, friendId);
        return userStorage.findCommonFriendsByIds(userId, friendId);
    }

    public Collection<User> getFriendsById(int id) {
        if (userStorage.findById(id).isEmpty()) {
            throw new UserNotFoundException(String.format(USER_NOT_EXISTS_MSG, id));
        }
        log.debug("Список друзей для пользователя с id = {} отправлен", id);
        return userStorage.findFriendsById(id);
    }

    public Collection<Film> getRecommendations(int userId) {
        log.debug("Список рекомендаций успешно выдан пользователю с id {}", userId);
        return filmStorage.getRecommendations(userId);
    }

    public Collection<FeedEvent> getFeedEvents(int id) {
        if (userStorage.findById(id).isEmpty()) {
            throw new UserNotFoundException(String.format(USER_NOT_EXISTS_MSG, id));
        }
        log.debug("Лента новостей для пользователя с id = {} отправлена", id);
        return feedEventStorage.findAllByUserId(id);
    }
}

