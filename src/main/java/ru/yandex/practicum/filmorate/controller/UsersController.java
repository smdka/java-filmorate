package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.FeedEvent;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.validation.Validator;

import javax.validation.Valid;
import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/users")
public class UsersController {
    private final UserService userService;

    @Autowired
    public UsersController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public Collection<User> findAll() {
        log.debug("Получен запрос GET /users");
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    public User getUserById(@PathVariable int id) {
        log.debug("Получен запрос GET /users/" + id);
        return userService.getUserById(id);
    }

    @GetMapping("/{id}/friends")
    public Collection<User> getFriends(@PathVariable int id) {
        log.debug("Получен запрос GET /users/{}/friends", id);
        return userService.getFriendsById(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public Collection<User> getCommonFriends(@PathVariable int id, @PathVariable int otherId) {
        log.debug("Получен запрос GET /users/{}/friends/common/{}", id, otherId);
        return userService.getCommonFriends(id, otherId);
    }

    @GetMapping("/{id}/recommendations")
    public Collection<Film> getRecommendations(@PathVariable int id) {
        log.debug("Получен запрос GET /users/{}/recommendations", id);
        return userService.getRecommendations(id);
    }


    @PostMapping
    public User add(@Valid @RequestBody User user, BindingResult bindingResult) {
        log.debug("Получен запрос POST /users/");
        Validator.ifHasErrorsThrowValidationException(bindingResult);
        return userService.add(user);
    }

    @PutMapping
    public User update(@Valid @RequestBody User newUser, BindingResult bindingResult) {
        log.debug("Получен запрос PUT /users/");
        Validator.ifHasErrorsThrowValidationException(bindingResult);
        return userService.update(newUser);
    }

    @PutMapping("/{userId}/friends/{friendId}")
    public void addFriend(@PathVariable int userId, @PathVariable int friendId) {
        ifNegativeThrowNotFoundException(friendId);
        log.debug("Получен запрос PUT /users/{}/friends/{}", userId, friendId);
        userService.sendFriendRequest(userId, friendId);
    }

    private void ifNegativeThrowNotFoundException(int friendId) {
        if (friendId <= 0) {
            throw new UserNotFoundException(String.format("Пользователь с id = %d не существует", friendId));
        }
    }

    @DeleteMapping("/{userId}/friends/{friendId}")
    public void deleteFriend(@PathVariable int userId, @PathVariable int friendId) {
        ifNegativeThrowNotFoundException(friendId);
        log.debug("Получен запрос DELETE /users/{}/friends/{}", userId, friendId);
        userService.deleteFriend(userId, friendId);
    }

    @DeleteMapping("/{userId}")
    public void delete(@PathVariable int userId) {
        log.debug("Получен запрос DELETE /users/{}", userId);
        ifNegativeThrowNotFoundException(userId);
        userService.deleteUserById(userId);
    }

    @GetMapping("/{id}/feed")
    public Collection<FeedEvent> getFeeds(@PathVariable int id){
        log.debug("Получен запрос GET /users/{}/feed", id);
        ifNegativeThrowNotFoundException(id);
        return userService.getFeedEvents(id);
    }
}
