package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;

@Service
@Slf4j
public class UserService {
    private static final String USER_NOT_EXISTS_MSG = "Пользователь с id = %d не существует";
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
        int id = newUser.getId();
        User updatedUser = storage.update(newUser)
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
        User user = getUserOrElseThrow(id);
        log.debug("Пользователь с id = {} успешно отправлен", id);
        return user;
    }

    private User getUserOrElseThrow(int id) {
        return storage.findById(id)
                .orElseThrow(() ->
                        new UserNotFoundException(String.format(USER_NOT_EXISTS_MSG, id)));
    }

    public Collection<User> getAllUsers() {
        log.debug("Список всех пользователей успешно отправлен");
        return storage.findAll();
    }

    public void deleteUserById(int id) {
        if (!storage.deleteById(id)) {
            throw new UserNotFoundException(String.format(USER_NOT_EXISTS_MSG, id));
        }
        log.debug("Пользователь с id = {} успешно удален", id);
    }

    public boolean sendFriendRequest(int fromUserId, int toUserId) {
        if (storage.addFriend(fromUserId, toUserId)) {
            log.debug("Пользователь с id = {} теперь в списке друзей пользователя с id = {}", toUserId, fromUserId);
            return true;
        }
        log.debug("Не удалось добавить пользователя с id = {} в список друзей пользователя с id = {}",
                  toUserId, fromUserId);
        return false;
    }

    public boolean deleteFriend(int userId, int friendId) {
        if (storage.removeFriend(userId, friendId)) {
            log.debug("Пользователь с id = {} удален из друзей пользователя с id = {}", friendId, userId);
            return true;
        }
        log.debug("Не удалось удалить пользователя с id = {} из списка друзей пользователя с id = {}",
                friendId, userId);
        return false;
    }

    public List<User> getCommonFriends(int  userId, int friendId) {
        log.debug("Список общих друзей для пользователя с id = {} и пользователя с id = {} отправлен",
                  userId, friendId);
        return (List<User>) storage.findCommonFriendsByIds(userId, friendId);
    }

    public List<User> getFriendsById(int id) {
        log.debug("Список друзей для пользователя с id = {} отправлен", id);
        return (List<User>) storage.findFriendsById(id);
    }
}

