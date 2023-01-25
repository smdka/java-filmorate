package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;

import javax.validation.Valid;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/users")
public class UsersController {
    private final UserService userService;
    private final FilmService filmService;

    @Autowired
    public UsersController(UserService userService, FilmService filmService) {
        this.userService = userService;
        this.filmService = filmService;
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
    public List<User> getFriends(@PathVariable int id) {
        log.debug("Получен запрос GET /users/{}/friends", id);
        return userService.getFriendsById(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public List<User> getCommonFriends(@PathVariable int id, @PathVariable int otherId) {
        log.debug("Получен запрос GET /users/{}/friends/common/{}", id, otherId);
        return userService.getCommonFriends(id, otherId);
    }

    @GetMapping("/{id}/recommendations")
    public List<Film> getRecommendations(@PathVariable int id) {
        log.debug("Получен запрос GET /users/{}/recommendations", id);
        return filmService.getRecommendations(id);
    }


    @PostMapping
    public User add(@Valid @RequestBody User user, BindingResult bindingResult) {
        log.debug("Получен запрос POST /users/");
        ifHasErrorsThrow(bindingResult);
        return userService.add(user);
    }

    private void ifHasErrorsThrow(BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            for (FieldError e : bindingResult.getFieldErrors()) {
                log.warn("Не пройдена валидация пользователя: {} = {}", e.getField(), e.getRejectedValue());
            }
            throw new ValidationException(bindingResult.getFieldErrors().toString());
        }
    }

    @PutMapping
    public User update(@Valid @RequestBody User newUser, BindingResult bindingResult) {
        log.debug("Получен запрос PUT /users/");
        ifHasErrorsThrow(bindingResult);
        return userService.update(newUser);
    }

    @PutMapping("/{userId}/friends/{friendId}")
    public void addFriend(@PathVariable int userId, @PathVariable int friendId) {
        ifNegativeThrow(friendId);
        log.debug("Получен запрос PUT /users/{}/friends/{}", userId, friendId);
        userService.sendFriendRequest(userId, friendId);
    }

    private void ifNegativeThrow(int friendId) {
        if (friendId <= 0) {
            throw new UserNotFoundException(String.format("Пользователь с id = %d не существует", friendId));
        }
    }

    @DeleteMapping("/{userId}/friends/{friendId}")
    public void deleteFriend(@PathVariable int userId, @PathVariable int friendId) {
        ifNegativeThrow(friendId);
        log.debug("Получен запрос DELETE /users/{}/friends/{}", userId, friendId);
        userService.deleteFriend(userId, friendId);
    }


}
