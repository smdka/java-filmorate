package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UserStorage {
    Collection<User> findAll();
    User save(User user);
    Optional<User> update(User user);
    boolean deleteById(int id);
    Optional<User> findById(int id);
    List<User> findFriendsById(int id);
    boolean addFriend(int userId, int friendId);
    boolean removeFriend(int userId, int friendId);
    List<User> findCommonFriendsByIds(int firstUserId, int secondUserId);
}
