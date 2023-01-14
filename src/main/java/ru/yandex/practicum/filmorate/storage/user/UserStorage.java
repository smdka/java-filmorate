package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.Optional;

public interface UserStorage {
    Collection<User> findAll();
    User save(User user);
    Optional<User> update(User user);
    boolean deleteById(int id);
    Optional<User> findById(int id);
    Collection<User> findFriendsById(int id);
    boolean addFriend(int userId, int friendId);
    boolean removeFriend(int userId, int friendId);
    Collection<User> findCommonFriendsByIds(int firstUserId, int secondUserId);
}
