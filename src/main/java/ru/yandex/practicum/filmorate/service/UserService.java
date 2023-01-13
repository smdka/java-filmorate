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
    private static final String FRIENDS_LIST_MSG = "Друзья пользователя с id = {}: {}";
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

    private static boolean isEmpty(String name) {
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

    public Map<User, User> sendFriendRequest(int fromUserId, int toUserId) {
        User user = getUserOrElseThrow(fromUserId);
        User friend = getUserOrElseThrow(toUserId);

        user.addFriend(friend);
        storage.update(user);
        log.debug("Пользователь с id = {} теперь в списке друзей пользователя с id = {}", toUserId, fromUserId);
        log.debug(FRIENDS_LIST_MSG, fromUserId, user.getFriendIds());
        log.debug(FRIENDS_LIST_MSG, toUserId, friend.getFriendIds());
        return Map.of(user, friend);
    }

    public User deleteFriend(int userId, int friendId) {
        User user = getUserOrElseThrow(userId);
        User friend = getUserOrElseThrow(friendId);

        user.deleteFriend(friend);
        storage.update(user);
        log.debug("Пользователь с id = {} удален из друзей пользователя с id = {}", friendId, userId);
        log.debug(FRIENDS_LIST_MSG, userId, user.getFriendIds());
        log.debug(FRIENDS_LIST_MSG, friendId, friend.getFriendIds());
        return user;
    }

    public List<User> getCommonFriends(int  userId, int friendId) {
        Collection<User> userFriends = storage.findFriendsById(userId);
        ifNullThrow(userId, userFriends);

        Collection<User> friendFriends = storage.findFriendsById(friendId);
        ifNullThrow(friendId, userFriends);

        userFriends.retainAll(friendFriends);
        log.debug("Список общих друзей для пользователя с id = {} и пользователя с id = {} отправлен",
                  userId, friendId);
        return new ArrayList<>(userFriends);
    }

    private void ifNullThrow(int id, Object o) {
        if (o == null) {
            throw new UserNotFoundException(String.format(USER_NOT_EXISTS_MSG, id));
        }
    }

    public List<User> getFriends(int id) {
        Collection<User> friends = storage.findFriendsById(id);
        ifNullThrow(id, friends);
        log.debug("Список друзей для пользователя с id = {} отправлен", id);
        return new ArrayList<>(friends);
    }
}

